package org.example.schedule.job;

import java.util.Date;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.example.elasticjob.starter.ElasticTask;

/**
 * @author JQ
 */
@ElasticTask(jobName = "testJob", cron = "*/5 * * * * ?", description = "自定義Task", overwrite = true)
public class TestJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("跑任務->" + new Date());
    }
}
