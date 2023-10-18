package user.registry.components.entities;


import kalix.javasdk.StatusCode;
import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.valueentity.ValueEntity;
import kalix.javasdk.valueentity.ValueEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import user.registry.Done;

@Id("id")
@TypeId("unique-address")
@RequestMapping("/unique-emails/{id}")
@Acl(allow = @Acl.Matcher(service = "*"))
public class UniqueEmailComponent extends ValueEntity<UniqueEmailComponent.UniqueEmail> {

  private final String address;
  private Logger logger = LoggerFactory.getLogger(getClass());

  public UniqueEmailComponent(ValueEntityContext context) {
    this.address = context.entityId();
  }

  public enum Status {
    NOT_USED,
    RESERVED,
    CONFIRMED
  }

  @Override
  public UniqueEmail emptyState() {
    return new UniqueEmail(address, Status.NOT_USED, null);
  }

  public record UniqueEmail(String address, Status status, String owner) {

    public UniqueEmail asConfirmed() {
      return new UniqueEmail(address, Status.CONFIRMED, owner);
    }

    public boolean isConfirmed() {
      return status == Status.CONFIRMED;
    }

    public boolean isInUse() {
      return status != Status.NOT_USED;
    }

    public boolean isUnused() {
      return status == Status.NOT_USED;
    }

    public boolean isReserved() {
      return status == Status.RESERVED;
    }
  }

  public record ReserveEmail(String address, String owner) {
  }

  @PostMapping
  public Effect<Done> reserve(@RequestBody ReserveEmail cmd) {
    logger.info("Reserving address '{}', current state is '{}'", cmd.address(), currentState());
    if (currentState().isInUse()) {
      return effects().error("Email already reserved");
    }
    return effects()
      .updateState(new UniqueEmail(cmd.address, Status.RESERVED, cmd.owner))
      .thenReply(Done.done());
  }

  @PostMapping("/confirm")
  public Effect<Done> confirm() {
    logger.info("Confirming address '{}'", currentState().address);
    if (currentState().isUnused()) {
      return effects().error("Email not in use");
    }
    if (currentState().isConfirmed()) {
      logger.info("Email is already confirmed. Ignoring confirmation request.");
      return effects().error("Email already confirmed");
    }
    return effects()
      .updateState(currentState().asConfirmed())
      .thenReply(Done.done());
  }

  @PostMapping("/unReserve")
  public Effect<Done> unReserve() {
    logger.info("Un-reserving address '{}'", currentState().address);
    if (currentState().isUnused()) {
      logger.info("Email not in use. Ignoring delete request.");
      return effects().reply(Done.done());

    } else if (currentState().isConfirmed()) {
      logger.info("Email is confirmed. Ignoring delete request.");
      return effects().reply(Done.done());

    } else {
      return effects()
        .updateState(emptyState())
        .thenReply(Done.done());
    }
  }

  @DeleteMapping()
  public Effect<Done> delete() {
    logger.info("Deleting address '{}'", currentState().address);
    return effects()
      .updateState(emptyState())
      .thenReply(Done.done());
  }


  @GetMapping
  public Effect<UniqueEmail> getState() {
    if (currentState() == null) {
      return effects().error("Email not found", StatusCode.ErrorCode.NOT_FOUND);
    }
    return effects().reply(currentState());
  }

}
