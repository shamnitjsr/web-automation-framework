package com.company.automation.listeners;

import com.company.automation.config.ConfigReader;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Splits the suite across N parallel runners (Kubernetes pods, Jenkins agents, ...).
 * Runner i of N runs only the test methods where hash(class#method) % N == i.
 * Configure with SHARD_TOTAL and SHARD_INDEX (0-based). Defaults (1, 0) run everything.
 * Every method lands in exactly one shard, and the split is stable between runs.
 */
public class ShardInterceptor implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        int total = ConfigReader.getInt("shard.total", 1);
        int index = ConfigReader.getInt("shard.index", 0);
        if (total <= 1) {
            return methods;
        }
        return methods.stream()
                .filter(m -> Math.floorMod(key(m).hashCode(), total) == index)
                .collect(Collectors.toList());
    }

    private static String key(IMethodInstance m) {
        return m.getMethod().getRealClass().getName() + "#" + m.getMethod().getMethodName();
    }
}
