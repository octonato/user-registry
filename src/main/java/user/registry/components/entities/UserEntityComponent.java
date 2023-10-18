package user.registry.components.entities;


import kalix.javasdk.StatusCode;
import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import user.registry.Done;
import user.registry.api.UserInfo;
import user.registry.domain.User;

@Id("id")
@TypeId("user")
@RequestMapping("/users/{id}")
@Acl(allow = @Acl.Matcher(service = "*"))
public class UserEntityComponent extends EventSourcedEntity<User, User.Event> {


  private Logger logger = LoggerFactory.getLogger(getClass());
  @PostMapping
  public Effect<Done> createUser(@RequestBody User.Create cmd) {

    logger.info("Creating user {}", cmd);
    if (currentState() != null) {
      return effects().error("User already created", StatusCode.ErrorCode.BAD_REQUEST);
    }
    return effects()
      .emitEvent(User.onCommand(cmd))
      .thenReply(__ -> Done.done());
  }

  @PutMapping("/change-email")
  public Effect<Done> changeEmail(@RequestBody User.ChangeEmail cmd) {
    if (currentState() == null) {
      return effects().error("User not found", StatusCode.ErrorCode.NOT_FOUND);
    }
    return currentState().onCommand(cmd)
      .fold(
        error -> effects().error(error),
        event -> effects().emitEvent(event).thenReply(__ -> Done.done())
      );
  }

  @PutMapping("/change-country")
  public Effect<Done> changeCountry(@RequestBody User.ChangeCountry cmd) {
    if (currentState() == null) {
      return effects().error("User not found", StatusCode.ErrorCode.NOT_FOUND);
    }
    return currentState().onCommand(cmd)
      .fold(
        error -> effects().error(error),
        event -> effects().emitEvent(event).thenReply(__ -> Done.done())
      );
  }

  @GetMapping
  public Effect<User> getState() {
    if (currentState() == null) {
      return effects().error("User not found", StatusCode.ErrorCode.NOT_FOUND);
    }
    return effects().reply(currentState());
  }

  @EventHandler
  public User onEvent(User.UserWasCreated evt) {
    return User.onEvent(evt);
  }

  @EventHandler
  public User onEvent(User.UsersEmailChanged evt) {
    return currentState().onEvent(evt);
  }

  @EventHandler
  public User onEvent(User.UsersCountryChanged evt) {
    return currentState().onEvent(evt);
  }
}
