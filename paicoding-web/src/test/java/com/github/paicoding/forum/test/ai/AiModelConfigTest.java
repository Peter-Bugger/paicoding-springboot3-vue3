package com.github.paicoding.forum.test.ai;

import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig;
import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig.ModelConf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiModelConfig 配置绑定测试
 * <p>
 * 覆盖：默认值 / enabled开关 / 多模型配置边界 / getter/setter
 * 风险等级: High — 正常路径 + 边界条件 + 错误路径 + 空值/空列表
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.1 — 多模型配置
// @PRD: US4 — AC4.4 — 新增模型配置
public class AiModelConfigTest {

    /**
     * 正常路径：默认值验证
     * @PRD: US4 — AC4.1 — enabled默认true，maxDailyRequests默认50
     */
    @Test
    public void testDefaultValues() {
        AiModelConfig config = new AiModelConfig();
        assertTrue(config.isEnabled());
        assertEquals(50, config.getMaxDailyRequests());
        assertNotNull(config.getModels());
        assertTrue(config.getModels().isEmpty());
    }

    /**
     * 正常路径：enabled开关设置
     * @PRD: US4 — AC4.1 — enabled=false时禁用功能
     */
    @Test
    public void testEnabledSwitch() {
        AiModelConfig config = new AiModelConfig();
        assertTrue(config.isEnabled());

        config.setEnabled(false);
        assertFalse(config.isEnabled());

        config.setEnabled(true);
        assertTrue(config.isEnabled());
    }

    /**
     * 正常路径：maxDailyRequests设置
     * @PRD: US1 — AC1.6 — 日限额可配置
     */
    @Test
    public void testMaxDailyRequests() {
        AiModelConfig config = new AiModelConfig();

        config.setMaxDailyRequests(100);
        assertEquals(100, config.getMaxDailyRequests());

        config.setMaxDailyRequests(0);
        assertEquals(0, config.getMaxDailyRequests());

        config.setMaxDailyRequests(-1);
        assertEquals(-1, config.getMaxDailyRequests());
    }

    /**
     * 正常路径：模型列表设置
     * @PRD: US4 — AC4.4 — 多模型可在列表中配置
     */
    @Test
    public void testModelList() {
        AiModelConfig config = new AiModelConfig();

        ModelConf model1 = createModelConf("deepseek", "DeepSeek V3", 1);
        ModelConf model2 = createModelConf("kimi", "月之暗面 Kimi", 2);

        config.setModels(List.of(model1, model2));
        assertEquals(2, config.getModels().size());
        assertEquals("deepseek", config.getModels().get(0).getName());
        assertEquals("月之暗面 Kimi", config.getModels().get(1).getDisplayName());
    }

    /**
     * 边界条件：空模型列表
     */
    @Test
    public void testEmptyModelList() {
        AiModelConfig config = new AiModelConfig();
        config.setModels(List.of());
        assertTrue(config.getModels().isEmpty());
    }

    /**
     * 边界条件：null模型列表
     */
    @Test
    public void testNullModelList() {
        AiModelConfig config = new AiModelConfig();
        config.setModels(null);
        assertNull(config.getModels());
    }

    /**
     * 边界条件：单个模型配置
     * @PRD: US4 — AC4.4 — 单模型也可正常工作
     */
    @Test
    public void testSingleModel() {
        AiModelConfig config = new AiModelConfig();
        ModelConf model = createModelConf("qwen", "通义千问", 1);
        config.setModels(List.of(model));

        assertEquals(1, config.getModels().size());
        ModelConf m = config.getModels().get(0);
        assertEquals("qwen", m.getName());
        assertEquals("通义千问", m.getDisplayName());
        assertTrue(m.isEnabled());
    }

    /**
     * 正常路径：ModelConf 默认值
     */
    @Test
    public void testModelConfDefaults() {
        ModelConf conf = new ModelConf();
        assertTrue(conf.isEnabled());
        assertEquals(4096, conf.getMaxTokens());
        assertEquals(30, conf.getTimeoutSeconds());
    }

    /**
     * 正常路径：ModelConf 完整设置
     * @PRD: US4 — AC4.4 — 模型配置完整字段
     */
    @Test
    public void testModelConfFullConfig() {
        ModelConf conf = new ModelConf();
        conf.setName("test-model");
        conf.setDisplayName("Test Model");
        conf.setPriority(0);
        conf.setApiHost("https://api.test.com");
        conf.setApiKey("sk-test-key");
        conf.setModelName("test-v1");
        conf.setMaxTokens(8192);
        conf.setTimeoutSeconds(60);
        conf.setEnabled(true);

        assertEquals("test-model", conf.getName());
        assertEquals("Test Model", conf.getDisplayName());
        assertEquals(0, conf.getPriority());
        assertEquals("https://api.test.com", conf.getApiHost());
        assertEquals("sk-test-key", conf.getApiKey());
        assertEquals("test-v1", conf.getModelName());
        assertEquals(8192, conf.getMaxTokens());
        assertEquals(60, conf.getTimeoutSeconds());
        assertTrue(conf.isEnabled());
    }

    /**
     * 边界条件：ModelConf disabled
     * @PRD: US4 — AC4.1 — 模型可单独禁用
     */
    @Test
    public void testModelConfDisabled() {
        ModelConf conf = new ModelConf();
        conf.setEnabled(false);
        assertFalse(conf.isEnabled());
    }

    /**
     * 边界条件：ModelConf maxTokens 最小值
     */
    @Test
    public void testModelConfMinMaxTokens() {
        ModelConf conf = new ModelConf();
        conf.setMaxTokens(1);
        assertEquals(1, conf.getMaxTokens());

        conf.setMaxTokens(0);
        assertEquals(0, conf.getMaxTokens());
    }

    // ==================== 工具方法 ====================

    private ModelConf createModelConf(String name, String displayName, int priority) {
        ModelConf conf = new ModelConf();
        conf.setName(name);
        conf.setDisplayName(displayName);
        conf.setPriority(priority);
        conf.setApiHost("https://api.example.com");
        conf.setApiKey("test-key");
        conf.setModelName(name + "-chat");
        conf.setMaxTokens(4096);
        conf.setTimeoutSeconds(30);
        conf.setEnabled(true);
        return conf;
    }
}
