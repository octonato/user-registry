package user.registry.components;

import kalix.javasdk.StatusCode;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import user.registry.Response;
import user.registry.domain.User;

@Id("id")
@TypeId("user")
@RequestMapping("/users/{id}")
public class UserEntityComponent extends EventSourcedEntity<User, User.Event> {


  @PostMapping
  public Effect<Response> createUser(@RequestBody User.Create cmd) {
    if (currentState() != null) {
      return effects().error("User already created", StatusCode.ErrorCode.BAD_REQUEST);
    }

    return effects()
        .emitEvent(User.onCommand(cmd))
        .thenReply(__ -> Response.done());
  }

  @PutMapping("/change-email")
  public Effect<Response> changeEmail(@RequestBody User.ChangeEmail cmd) {
    if (currentState() == null) {
      return effects().error("User not found", StatusCode.ErrorCode.NOT_FOUND);
    }
    return currentState().onCommand(cmd)
      .fold(
        error -> effects().error(error),
        event -> effects().emitEvent(event).thenReply(__ -> Response.done())
      );
  }

  @EventHandler
  public User onEvent(User.Created evt) {
    return User.onEvent(evt);
  }

  @EventHandler
  public User onEvent(User.EmailChanged evt) {
    return currentState().onEvent(evt);
  }

}
