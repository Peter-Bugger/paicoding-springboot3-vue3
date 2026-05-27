package com.github.paicoding.forum.service.chatai.service.ai;

import com.github.paicoding.forum.api.model.vo.ai.ModelInfoVO;
import com.github.paicoding.forum.service.chatai.service.ai.AiModelConfig.ModelConf;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI 模型路由器
 * <p>
 * 负责：
 * - 根据 AiModelConfig 创建 OpenAiCompatibleProvider 实例
 * - 按优先级选择可用模型
 * - 调用失败时自动降级到下一个可用模型
 * - 定时健康检查，恢复不可用模型
 *
 * @author XuYifei
 * @date 2024-07-12
 */
// @PRD: US4 — AC4.1 — 多模型配置，按优先级选择
// @PRD: US4 — AC4.2 — 失败切换降级
// @PRD: US4 — AC4.3 — 全部不可用时返回 503
// @PRD: US4 — AC4.5 — 健康检查，多次失败标记冷却
@Slf4j
@Component
public class AiModelRouter {

    /**
     * 所有 Provider 列表，按 priority 排序
     */
    private final List<AiModelProvider> providers = new ArrayList<>();

    /**
     * Provider 名称到实例的映射，便于查找
     */
    private final Map<String, AiModelProvider> providerMap = new ConcurrentHashMap<>();

    @Autowired
    private AiModelConfig aiModelConfig;

    @PostConstruct
    public void init() {
        if (!aiModelConfig.isEnabled()) {
            log.warn("AI 解读功能已禁用（ai-interpret.enabled=false）");
            return;
        }
        List<ModelConf> models = aiModelConfig.getModels();
        if (models == null || models.isEmpty()) {
            log.warn("AI 解读功能未配置任何模型（ai-interpret.models 为空）");
            return;
        }
        for (ModelConf conf : models) {
            if (!conf.isEnabled()) {
                log.info("模型 {} 已禁用，跳过", conf.getName());
                continue;
            }
            OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(conf);
            providers.add(provider);
            providerMap.put(conf.getName(), provider);
            log.info("注册 AI 模型: {} ({})，优先级={}", conf.getName(), conf.getDisplayName(), conf.getPriority());
        }
        // 按优先级排序（数值越小越优先）
        providers.sort(Comparator.comparingInt(AiModelProvider::getPriority));
        log.info("AI 模型路由器初始化完成，共 {} 个可用模型", providers.size());
    }

    /**
     * 选择一个可用模型
     * 按优先级排序，返回第一个 isAvailable()=true 的 Provider
     *
     * @return 可用模型，或 null（全部不可用）
     */
    public AiModelProvider selectProvider() {
        return providers.stream()
                .filter(AiModelProvider::isAvailable)
                .findFirst()
                .orElse(null);
    }

    /**
     * 标记指定 Provider 不可用，并返回下一个可用 Provider
     *
     * @param failed 调用失败的 Provider
     * @return 下一个可用模型，或 null（全部不可用）
     */
    public AiModelProvider fallbackToNext(AiModelProvider failed) {
        if (failed != null) {
            failed.markUnavailable();
        }
        return selectProvider();
    }

    /**
     * @return 是否存在可用模型
     */
    public boolean hasAvailableProvider() {
        return providers.stream().anyMatch(AiModelProvider::isAvailable);
    }

    /**
     * 列出所有可用模型的信息
     *
     * @return 模型信息列表
     */
    public List<ModelInfoVO> listAvailableModels() {
        return providers.stream().map(p -> {
            ModelInfoVO vo = new ModelInfoVO();
            vo.setName(p.getProviderName());
            vo.setDisplayName(p.getDisplayName());
            vo.setAvailable(p.isAvailable());
            vo.setPriority(p.getPriority());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取指定名称的 Provider
     *
     * @param name 模型名称
     * @return Provider 实例，或 null
     */
    public AiModelProvider getProvider(String name) {
        return providerMap.get(name);
    }

    /**
     * 定时健康检查，每 5 分钟执行一次
     * 对标记为不可用的 Provider 发送最小健康探测请求
     */
    @Scheduled(fixedDelay = 300_000)
    public void checkHealth() {
        for (AiModelProvider provider : providers) {
            if (!provider.isAvailable() && provider instanceof OpenAiCompatibleProvider) {
                OpenAiCompatibleProvider compatibleProvider = (OpenAiCompatibleProvider) provider;
                try {
                    boolean healthy = compatibleProvider.healthCheck();
                    if (healthy) {
                        log.info("健康检查通过，恢复模型: {}", provider.getProviderName());
                        provider.markAvailable();
                    } else {
                        log.warn("健康检查失败，模型仍不可用: {}", provider.getProviderName());
                    }
                } catch (Exception e) {
                    log.warn("健康检查异常，模型仍不可用: {} - {}",
                            provider.getProviderName(), e.getMessage());
                }
            }
        }
    }
}
