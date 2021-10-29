package scheduleds;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import utils.ConnectTestFunctions;

@Component
public class ConnectSchedule01 {

	private static final Logger log = LoggerFactory.getLogger(ConnectSchedule01.class);

	public static int count = 0;

	@Value("${connection.target:#{null}}")
	String connectionTarget;

	@Value("${connection.Probability:#{1}}")
	Float connectionProbability;

	@Value("${connection.enabled:false}")
	boolean connectionEnabled;

	@PostConstruct
	@Scheduled(cron = "* * * * * *")
	public void connect01() {

		// float p = 0.3f;
		float p = connectionProbability / 100;

		try {
			if (connectionEnabled && connectionTarget != null) {

				Random random = new Random();

				float randomFloatResult = random.nextFloat();

				// boolean randomResult = random.nextFloat() < p;
				boolean randomResult = randomFloatResult < p;
				log.info("randomFloatResult: " + String.valueOf(randomFloatResult) + " randomResult: " + randomResult);

				if (randomResult) {
					ConnectTestFunctions.http(connectionTarget, "GET", 10, 10);
//					ConnectTestFunctions.http(connectionTarget + count, "GET", 10, 10);
//					count++;
				}
			}
		} catch (Exception e) {
			log.error("count: " + String.valueOf(count));
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}

	}

}
