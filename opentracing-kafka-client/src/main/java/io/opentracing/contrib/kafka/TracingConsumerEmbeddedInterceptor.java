/*
 * Copyright 2017-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.kafka;

import io.opentracing.Tracer;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class TracingConsumerEmbeddedInterceptor<K, V> implements ConsumerInterceptor<K, V> {

  private Map<String, Tracer> tracerMapping;

  @Override
  public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {

    Tracer tracer = null;

    for (ConsumerRecord<K, V> record : records) {

      tracer = tracerMapping.get(record.topic());

      if (tracer != null) {

        TracingKafkaUtils.buildAndFinishChildSpan(record, tracer);

      }

    }

    return records;

  }

  @Override
  public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {

  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> configs) {

    if (configs.containsKey(TracingKafkaUtils.CONFIG_FILE_PROP)) {

      String configFileName = (String) configs.get(TracingKafkaUtils.CONFIG_FILE_PROP);
      tracerMapping = TracingKafkaUtils.buildTracerMapping(configFileName);
 
    }

  }

}