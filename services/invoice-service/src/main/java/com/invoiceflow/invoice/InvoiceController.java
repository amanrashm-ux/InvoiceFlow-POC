package com.invoiceflow.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceflow.common.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/invoices")
@CrossOrigin(origins="*")
public class InvoiceController {
  private final JdbcTemplate jdbc; private final JdbcTemplate readJdbc; private final ObjectMapper mapper;
  public InvoiceController(JdbcTemplate jdbc,@org.springframework.beans.factory.annotation.Qualifier("readJdbcTemplate") JdbcTemplate readJdbc,ObjectMapper mapper){this.jdbc=jdbc;this.readJdbc=readJdbc;this.mapper=mapper;}
  public record InvoiceRequest(String supplierId,String buyerId,String invoiceNumber,String currency,BigDecimal amount,LocalDate issueDate,LocalDate dueDate){}

  @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
  public Map<String,Object> create(@RequestBody InvoiceRequest r) {
    if(r.supplierId()==null||r.buyerId()==null||r.invoiceNumber()==null||r.currency()==null||r.amount()==null||r.issueDate()==null||r.dueDate()==null)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"All invoice fields are required");
    if(r.amount().signum()<=0 || r.dueDate().isBefore(r.issueDate()) || r.currency().length()!=3)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid amount, dates or currency");
    String id="INV-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
    jdbc.update("INSERT INTO invoices(invoice_id,supplier_id,buyer_id,invoice_number,currency,amount,issue_date,due_date,status) VALUES(?,?,?,?,?,?,?,?,?)",
      id,r.supplierId(),r.buyerId(),r.invoiceNumber(),r.currency().toUpperCase(),r.amount(),r.issueDate(),r.dueDate(),"SUBMITTED");
    Map<String,Object> payload=new LinkedHashMap<>(); payload.put("invoiceId",id);payload.put("supplierId",r.supplierId());payload.put("buyerId",r.buyerId());payload.put("invoiceNumber",r.invoiceNumber());payload.put("currency",r.currency().toUpperCase());payload.put("amount",r.amount());payload.put("issueDate",r.issueDate().toString());payload.put("dueDate",r.dueDate().toString());
    writeOutbox("invoice.created",DomainEvent.of("invoice.created",id,"invoice-service",payload));
    return Map.of("invoiceId",id,"status","SUBMITTED","message","Invoice submitted; event queued for publication");
  }

  @PostMapping("/{id}/accept") @Transactional
  public Map<String,Object> accept(@PathVariable String id) {
    List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM invoices WHERE invoice_id=?",id);
    if(rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invoice not found");
    String status=(String)rows.get(0).get("status");
    if("ACCEPTED".equals(status)) return Map.of("invoiceId",id,"status",status,"message","Already accepted");
    if(!"SUBMITTED".equals(status)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Invoice cannot be accepted from status "+status);
    jdbc.update("UPDATE invoices SET status='ACCEPTED',updated_at=CURRENT_TIMESTAMP WHERE invoice_id=?",id);
    Map<String,Object> old=rows.get(0); Map<String,Object> payload=new LinkedHashMap<>();
    for(String k:List.of("invoice_id","supplier_id","buyer_id","invoice_number","currency","amount","issue_date","due_date")) payload.put(switch(k){case "invoice_id"->"invoiceId";case "supplier_id"->"supplierId";case "buyer_id"->"buyerId";case "invoice_number"->"invoiceNumber";case "issue_date"->"issueDate";case "due_date"->"dueDate";default->k;},old.get(k));
    writeOutbox("invoice.accepted",DomainEvent.of("invoice.accepted",id,"invoice-service",payload));
    return Map.of("invoiceId",id,"status","ACCEPTED","message","Buyer acceptance recorded");
  }

  @GetMapping public List<Map<String,Object>> list(){return readJdbc.queryForList("SELECT invoice_id AS \"invoiceId\",supplier_id AS \"supplierId\",buyer_id AS \"buyerId\",invoice_number AS \"invoiceNumber\",currency,amount,issue_date AS \"issueDate\",due_date AS \"dueDate\",status,created_at AS \"createdAt\" FROM invoices ORDER BY created_at DESC");}
  @GetMapping("/{id}") public Map<String,Object> get(@PathVariable String id){return readJdbc.queryForList("SELECT * FROM invoices WHERE invoice_id=?",id).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
  private void writeOutbox(String key,DomainEvent event){try{jdbc.update("INSERT INTO outbox_events(event_id,routing_key,event_json) VALUES(?,?,?)",event.eventId(),key,mapper.writeValueAsString(event));}catch(Exception e){throw new IllegalStateException("Could not serialize event",e);}}
}
