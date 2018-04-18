package com.kafkastream;
import com.kafkastream.model.Customer;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestProducer
{

    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        // When configuring the default serdes of StreamConfig
        Properties properties = new Properties();
        properties.put("applications.id","cqrs-streams");
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put("schema.registry.url", "http://localhost:8081/");
        properties.put("acks", "all");
        properties.put("key.serializer", SpecificAvroSerializer.class);
        properties.put("value.serializer", SpecificAvroSerializer.class);

        // When you want to override serdes explicitly/selectively
        SpecificAvroSerde<Customer> customerSerde = createSerde("http://localhost:8081/");
        Producer<String,Customer>   producer=new KafkaProducer<>(properties,Serdes.String().serializer(),customerSerde.serializer());

        //Create Customer
        Customer customer=new Customer();
        customer.setCustomerId("CU1001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@gmail.com");
        customer.setPhone("993-332-9832");

        ProducerRecord<String, Customer> customerRecord = new ProducerRecord<>("customer", customer.getCustomerId(), customer);
        Future<RecordMetadata> future = producer.send(customerRecord);
        System.out.println("Customer record sent. Customer Id: " + customer.getCustomerId());
        System.out.println("Customer future.get(): " + future.get());
    }
    private static <VT extends SpecificRecord> SpecificAvroSerde<VT> createSerde(final String schemaRegistryUrl)
    {

        final SpecificAvroSerde<VT> serde = new SpecificAvroSerde<>();
        final Map<String, String> serdeConfig = Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        serde.configure(serdeConfig, false);
        return serde;
    }

}