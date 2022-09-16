# 分散式任務排程系統
>此專案要先使用 **asandy1023/elasticjob-springboot-starter** 這個專案推到maven私庫中。然後在此專案中引入我們的starter依賴，使用interface @ElasticTask(jobName = "{jobName}", cron = "*/5 * * * * ?", description = "{description}", overwrite = true) 來進行操作任務排程
