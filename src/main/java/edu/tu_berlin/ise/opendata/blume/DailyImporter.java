package edu.tu_berlin.ise.opendata.blume;

import com.google.gson.JsonElement;
import edu.tu_berlin.ise.opendata.blume.model.DailyMeasurements;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Created by aardila on 2017-07/03.
 */
public class DailyImporter {

    private static final String IMPORTER_ID = "BLUME";
    private static final String KAFKA_SERVER_ENV_VAR_KEY = "KAFKA_HOST";
    private static final String KAFKA_TOPIC_ENV_VAR_KEY = "KAFKA_TOPIC";

    private static final Logger logger = LoggerFactory.getLogger(BlumeWrapper.class);

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            throw new Exception("Mandatory import date argument missing!");
        }

        final String kafkaServer = System.getenv(KAFKA_SERVER_ENV_VAR_KEY);

        if (kafkaServer == null || kafkaServer.isEmpty()) {
            throw new Exception(
                    String.format("Mandatory environment variable %s was not set", KAFKA_SERVER_ENV_VAR_KEY));
        }

        String kafkaTopic = System.getenv(KAFKA_TOPIC_ENV_VAR_KEY);
        if (kafkaTopic == null || kafkaTopic.isEmpty()) {
            //if no topic env var exists, use the importer ID as the topic name by default
            logger.warn("Environment variable {} not present. Defaulting to {}", KAFKA_TOPIC_ENV_VAR_KEY, IMPORTER_ID);
            kafkaTopic = IMPORTER_ID;
        }

//        System.out.println("Waiting for Kafka to spin up. Sleeping for 20 s...");
//        Thread.sleep(20000);

        String dateArg = args[0];
        LocalDate dateToImport = LocalDate.parse(dateArg);
        logger.info("Downloading measurements... ");
        DailyMeasurements dailyMeasurements = BlumeWrapper.getDailyMeasurements(dateToImport);
        logger.info("Done!");

        System.out.println("Serializing... ");
        JsonElement measurementsJsonArray = Serializer.serializeDailyMeasurements(dailyMeasurements);
        logger.info("Done!");

        logger.info("Sending json to Kafka queue on " + kafkaServer);
        ArrayList<Future<RecordMetadata>> futures = new ArrayList<Future<RecordMetadata>>(dailyMeasurements.stationMeasurements.size());
        KafkaQueue kafkaQueue = new KafkaQueue(kafkaTopic, kafkaServer);

        for (JsonElement jsonElement : measurementsJsonArray.getAsJsonArray()) {
            String jsonPayload = jsonElement.toString();
            Future<RecordMetadata> future = kafkaQueue.publish(jsonPayload);
            futures.add(future);
        }

        logger.info("Done!");

        logger.info("Waiting for Kafka to acknowledge...");
        for (Future<RecordMetadata> f : futures) {
            RecordMetadata rm = f.get();
            logger.info("Topic: {}. Offset: {}. Partition: {}", rm.topic(), rm.offset(), rm.partition());
        }

        logger.info("Done importing! Tschüss!");
    }
}
