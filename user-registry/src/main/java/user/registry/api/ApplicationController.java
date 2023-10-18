package user.registry.api;


import kalix.javasdk.action.Action;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import user.registry.Done;
import user.registry.components.entities.UserEntityComponent;
import user.registry.domain.User;

@RequestMapping("/api")
public class ApplicationController extends Action {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private final ComponentClient client;

  public ApplicationController(ComponentClient client) {
    this.client = client;
  }

  @PostMapping("/users/{userId}")
  public Effect<Done> createUser(@PathVariable String userId, @RequestBody User.Create cmd) {

    // call is lazy, not yet executed
    var callToUser =
      client
        .forEventSourcedEntity(userId)
        .call(UserEntityComponent::createUser)
        .params(cmd);

    return effects().forward(callToUser);

  }


  @PutMapping("/users/{userId}/change-email")
  public Effect<Done> changeEmail(@PathVariable String userId, @RequestBody User.ChangeEmail cmd) {

    // call is lazy, not yet executed
    var callToUser =
      client
        .forEventSourcedEntity(userId)
        .call(UserEntityComponent::changeEmail)
        .params(cmd);

    return effects().forward(callToUser);

  }


  @GetMapping("/users/{userId}")
  public Effect<UserInfo> getUserInfo(@PathVariable String userId) {

    var res =
      client.forEventSourcedEntity(userId)
        .call(UserEntityComponent::getState)
        .execute()
        .thenApply(user -> {
          var userInfo =
            new UserInfo(
              userId,
              user.name,
              user.country,
              user.email);

          logger.info("UserInfo {}", userInfo);
          return userInfo;
        });

    return effects().asyncReply(res);
  }


}
