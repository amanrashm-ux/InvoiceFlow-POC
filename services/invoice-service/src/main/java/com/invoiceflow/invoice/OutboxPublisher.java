package com.invoiceflow.invoice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceflow.common.DomainEvent;
import com.invoiceflow.common.EventPublisher;
import java.util.List;import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.scheduling.annotation.*;import org.springframework.stereotype.Component;
@Component @EnableScheduling
public class OutboxPublisher {
 private final JdbcTemplate jdbc; private final ObjectMapper mapper; private final EventPublisher publisher;
 public OutboxPublisher(JdbcTemplate j,ObjectMapper m,EventPublisher p){jdbc=j;mapper=m;publisher=p;}
 @Scheduled(fixedDelay=1000) public void publishPending(){
  List<Map<String,Object>> pending=jdbc.queryForList("SELECT event_id,routing_key,event_json FROM outbox_events WHERE published_at IS NULL ORDER BY created_at LIMIT 100");
  for(Map<String,Object> row:pending){try{DomainEvent e=mapper.readValue((String)row.get("event_json"),DomainEvent.class);publisher.publish((String)row.get("routing_key"),e);jdbc.update("UPDATE outbox_events SET published_at=CURRENT_TIMESTAMP WHERE event_id=?",row.get("event_id"));}catch(Exception ex){System.err.println("Outbox publish failed for "+row.get("event_id")+": "+ex.getMessage());}}
 }
}
