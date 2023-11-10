package user.registry.api;


import kalix.javasdk.StatusCode;
import kalix.javasdk.action.Action;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import user.registry.Done;
import user.registry.entities.UniqueEmailEntity;
import user.registry.entities.UserEntity;

@RequestMapping("/api")
public class ApplicationController extends Action {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ComponentClient client;

  public ApplicationController(ComponentClient client) {
    this.client = client;
  }

  @PostMapping("/users/{userId}")
  public Effect<Done> createUser(@PathVariable String userId, @RequestBody UserEntity.Create cmd) {

    var createUniqueEmail = new UniqueEmailEntity.ReserveEmail(cmd.email(), userId);

    logger.info("Reserving new address '{}'", cmd.email());
    var emailReserved =
      client
        .forValueEntity(cmd.email())
        .call(UniqueEmailEntity::reserve)
        .params(createUniqueEmail)
        .execute(); // eager, executing it now

    // call is lazy, not yet executed
    var callToUser =
      client
        .forEventSourcedEntity(userId)
        .call(UserEntity::createUser)
        .params(cmd);


    var userCreated =
      emailReserved
        .thenApply(__ -> {
          logger.info("Creating user '{}'", userId);
          return effects().asyncReply(callToUser.execute());
        })
        .exceptionally(e -> {
          logger.info("Email already reserved '{}'", cmd.email());
          return effects().error("Email already reserved '" + cmd.email() + "'", StatusCode.ErrorCode.BAD_REQUEST);
        });

    return effects().asyncEffect(userCreated);
  }


  @PutMapping("/users/{userId}/change-email")
  public Effect<Done> changeEmail(@PathVariable String userId, @RequestBody UserEntity.ChangeEmail cmd) {

    var createUniqueEmail = new UniqueEmailEntity.ReserveEmail(cmd.newEmail(), userId);

    logger.info("Reserving new address '{}'", cmd.newEmail());
    var emailReserved =
      client
        .forValueEntity(cmd.newEmail())
        .call(UniqueEmailEntity::reserve)
        .params(createUniqueEmail)
        .execute(); // eager, executing it now

    // call is lazy, not yet executed
    var callToUser =
      client
        .forEventSourcedEntity(userId)
        .call(UserEntity::changeEmail)
        .params(cmd);


    var userCreated =
      emailReserved
        .thenApply(__ -> {
          logger.info("Changing user's address '{}'", userId);
          return effects().asyncReply(callToUser.execute());
        })
        .exceptionally(e -> {
          logger.info("Email already reserved '{}'", e.getMessage());
          return effects().error(e.getMessage(), StatusCode.ErrorCode.BAD_REQUEST);
        });

    return effects().asyncEffect(userCreated);

  }


  @GetMapping("/users/{userId}")
  public Effect<UserInfo> getUserInfo(@PathVariable String userId) {

    var res =
      client.forEventSourcedEntity(userId)
        .call(UserEntity::getState)
        .execute()
        .thenApply(user -> {
          var userInfo =
            new UserInfo(
              userId,
              user.name(),
              user.country(),
              user.email());

          logger.info("Getting user info: {}", userInfo);
          return userInfo;
        });

    return effects().asyncReply(res);
  }

  @GetMapping("/emails/{address}")
  public Effect<EmailInfo> getEmailInfo(@PathVariable String address) {
    var res =
      client.forValueEntity(address)
        .call(UniqueEmailEntity::getState)
        .execute()
        .thenApply(email -> {
          var emailInfo =
            new EmailInfo(
              email.address(),
              email.status().toString(),
              email.ownerId());

          logger.info("Getting email info: {}", emailInfo);
          return emailInfo;
        });

    return effects().asyncReply(res);
  }

}
