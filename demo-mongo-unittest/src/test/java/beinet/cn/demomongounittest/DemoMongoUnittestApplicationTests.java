package beinet.cn.demomongounittest;

import beinet.cn.demomongounittest.entity.Users;
import beinet.cn.demomongounittest.repository.UsersRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("unittest")
class DemoMongoUnittestApplicationTests {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;

    /**
     * 启动mock的mongo
     * 注意：首次启动测试，会在宿主机下载相应版本的Mongo，所以会比较慢。
     *
     * @throws Exception 可能的异常
     */
    @PostConstruct
    void startMongo() throws Exception {
        // 超过3.4的以上版本，会报错： Could not start process: <EOF>
        mongodExecutable = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.V3_4)
                .net(new Net(12345, Network.localhostIsIPv6())).build());
        mongod = mongodExecutable.start();
    }

    /**
     * 停止mock的mongo
     */
    @PreDestroy
    void stopMongo() {
        mongod.stop();
        mongodExecutable.stop();
    }

    @Autowired
    UsersRepository repository;

    @Test
    void contextLoads() {
        System.out.println("run方法启动...");

        // 因为是真实的mongo，所以之前的测试记录会保留，建议先清理
        long cnt = repository.count();
        System.out.println("总记录数： " + cnt);

        repository.deleteAll();

        Users item = repository.findTopByNameOrderByIdDesc("张三");
        Assert.isTrue(item == null, "不应该有数据啊");

        item = Users.builder()
                .id(12L)
                .name("张三")
                .birthday(LocalDateTime.of(2001, 11, 30, 23, 59))
                .gender(2)
                .desc("插入")
                .build();
        repository.insert(item);

        item = repository.findTopByNameOrderByIdDesc("张三");
        Assert.isTrue(item.getDesc().equals("插入"), "应该有数据了啊");

        item.setDesc(item.getDesc() + "-更新");
        repository.save(item);

        item = repository.findTopByNameOrderByIdDesc("张三");
        Assert.isTrue(item.getDesc().equals("插入-更新"), "应该有数据了啊");
    }

}
