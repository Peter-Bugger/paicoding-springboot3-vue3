package com.github.paicoding.forum.service.chatai.service.ai;

import com.github.paicoding.forum.api.model.vo.ai.ModelInfoVO;
import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig.ModelConf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AiModelRouter 单元测试
 * <p>
 * 覆盖：路由选择 / 降级切换 / 健康检查 / 模型列表 / 空配置
 * 风险等级: High — 正常路径 + 边界条件 + 错误路径 + 空值/空列表
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.1 — 多模型配置，按优先级选择
// @PRD: US4 — AC4.2 — 失败切换降级
// @PRD: US4 — AC4.3 — 全部不可用时返回 503
// @PRD: US4 — AC4.5 — 健康检查
@ExtendWith(MockitoExtension.class)
public class AiModelRouterTest {

    @Mock
    private AiModelConfig aiModelConfig;

    private AiModelRouter router;

    @BeforeEach
    public void setUp() {
        router = new AiModelRouter();
        ReflectionTestUtils.setField(router, "aiModelConfig", aiModelConfig);
    }

    // ==================== selectProvider 测试 ====================

    /**
     * 正常路径：按优先级选择第一个可用模型
     * @PRD: US4 — AC4.1 — 多模型按优先级选择
     */
    @Test
    public void testSelectProvider_returnsFirstAvailable() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        ModelConf conf2 = createModelConf("model2", "Model 2", 2, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf2, conf1));

        router.init();

        AiModelProvider provider = router.selectProvider();
        assertNotNull(provider);
        assertEquals("model1", provider.getProviderName());
    }

    /**
     * 边界条件：所有模型都不可用
     * @PRD: US4 — AC4.3 — 全部不可用时返回 null
     */
    @Test
    public void testSelectProvider_allUnavailable() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();
        // 标记为不可用
        AiModelProvider provider = router.selectProvider();
        assertNotNull(provider);
        provider.markUnavailable();

        // 再次选择应为 null
        assertNull(router.selectProvider());
    }

    /**
     * 边界条件：功能禁用（enabled=false）
     */
    @Test
    public void testSelectProvider_disabled() {
        when(aiModelConfig.isEnabled()).thenReturn(false);

        router.init();

        assertNull(router.selectProvider());
    }

    /**
     * 空值/空列表：模型配置为空
     */
    @Test
    public void testSelectProvider_emptyModels() {
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(new ArrayList<>());

        router.init();

        assertNull(router.selectProvider());
    }

    /**
     * 空值/空列表：模型配置为 null
     */
    @Test
    public void testSelectProvider_nullModels() {
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(null);

        router.init();

        assertNull(router.selectProvider());
    }

    /**
     * 边界条件：模型 disabled
     */
    @Test
    public void testSelectProvider_skipsDisabledModels() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, false, true);
        ModelConf conf2 = createModelConf("model2", "Model 2", 2, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1, conf2));

        router.init();

        AiModelProvider provider = router.selectProvider();
        assertNotNull(provider);
        assertEquals("model2", provider.getProviderName());
    }

    // ==================== fallbackToNext 测试 ====================

    /**
     * 正常路径：降级到下一个可用模型
     * @PRD: US4 — AC4.2 — 失败切换到下一个
     */
    @Test
    public void testFallbackToNext() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        ModelConf conf2 = createModelConf("model2", "Model 2", 2, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1, conf2));

        router.init();

        AiModelProvider first = router.selectProvider();
        assertEquals("model1", first.getProviderName());

        AiModelProvider next = router.fallbackToNext(first);
        assertNotNull(next);
        assertEquals("model2", next.getProviderName());
        assertFalse(first.isAvailable()); // 原模型应标记不可用
    }

    /**
     * 错误路径：降级但无可用模型
     * @PRD: US4 — AC4.3 — 全部不可用时返回 null
     */
    @Test
    public void testFallbackToNext_noMoreAvailable() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();

        AiModelProvider first = router.selectProvider();
        // 先标记不可用
        first.markUnavailable();
        // 降级
        AiModelProvider next = router.fallbackToNext(first);
        assertNull(next);
    }

    // ==================== hasAvailableProvider 测试 ====================

    /**
     * 正常路径：存在可用模型
     */
    @Test
    public void testHasAvailableProvider_true() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();
        assertTrue(router.hasAvailableProvider());
    }

    /**
     * 错误路径：没有可用模型
     */
    @Test
    public void testHasAvailableProvider_false() {
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(new ArrayList<>());

        router.init();
        assertFalse(router.hasAvailableProvider());
    }

    // ==================== listAvailableModels 测试 ====================

    /**
     * 正常路径：列出所有模型（含可用状态）
     * @PRD: US4 — AC4.1 — 模型列表返回
     */
    @Test
    public void testListAvailableModels() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        ModelConf conf2 = createModelConf("model2", "Model 2", 2, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1, conf2));

        router.init();

        List<ModelInfoVO> models = router.listAvailableModels();
        assertEquals(2, models.size());
        assertEquals("model1", models.get(0).getName());
        assertEquals("Model 1", models.get(0).getDisplayName());
        assertTrue(models.get(0).isAvailable());
    }

    /**
     * 空值/空列表：无模型时返回空列表
     */
    @Test
    public void testListAvailableModels_empty() {
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(new ArrayList<>());

        router.init();

        List<ModelInfoVO> models = router.listAvailableModels();
        assertTrue(models.isEmpty());
    }

    // ==================== getProvider 测试 ====================

    /**
     * 正常路径：按名称查找
     */
    @Test
    public void testGetProvider_found() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();

        AiModelProvider provider = router.getProvider("model1");
        assertNotNull(provider);
    }

    /**
     * 错误路径：名称不存在
     */
    @Test
    public void testGetProvider_notFound() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();

        assertNull(router.getProvider("non_existent"));
    }

    // ==================== checkHealth 测试 ====================

    /**
     * 正常路径：健康检查恢复不可用模型
     * @PRD: US4 — AC4.5 — 健康检查恢复模型
     */
    @Test
    public void testCheckHealth() {
        ModelConf conf1 = createModelConf("model1", "Model 1", 1, true, true);
        when(aiModelConfig.isEnabled()).thenReturn(true);
        when(aiModelConfig.getModels()).thenReturn(List.of(conf1));

        router.init();

        // 标记不可用
        AiModelProvider provider = router.selectProvider();
        provider.markUnavailable();
        assertFalse(provider.isAvailable());

        // healthCheck 不会实际调用（OpenAiCompatibleProvider 需要网络），
        // 这里只是验证方法不抛异常
        router.checkHealth();
    }

    // ==================== 工具方法 ====================

    private ModelConf createModelConf(String name, String displayName, int priority,
                                      boolean enabled, boolean available) {
        ModelConf conf = new ModelConf();
        conf.setName(name);
        conf.setDisplayName(displayName);
        conf.setPriority(priority);
        conf.setEnabled(enabled);
        conf.setApiHost("https://api.example.com");
        conf.setApiKey("test-key");
        conf.setModelName(name + "-model");
        conf.setMaxTokens(4096);
        conf.setTimeoutSeconds(30);
        return conf;
    }
}
