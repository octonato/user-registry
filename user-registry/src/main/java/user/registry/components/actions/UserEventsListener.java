package user.registry.components.actions;


import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.registry.Done;
import user.registry.components.entities.UniqueEmailComponent;
import user.registry.components.entities.UserEntityComponent;
import user.registry.domain.User;

@Subscribe.EventSourcedEntity(value = UserEntityComponent.class, ignoreUnknown = true)
public class UserEventsListener extends Action {

  private final ComponentClient client;
  private Logger logger = LoggerFactory.getLogger(getClass());

  private UserEventsListener(ComponentClient client) {
    this.client = client;
  }

  public Effect<Done> onEvent(User.UserWasCreated evt) {
    logger.info("User was created: {}, confirming new address address", evt);
    var confirmation = client.forValueEntity(evt.email()).call(UniqueEmailComponent::confirm);
    return effects().forward(confirmation);
  }

  public Effect<Done> onEvent(User.UsersEmailChanged evt) {
    logger.info("User go a new address assigned: {}, confirming new address address", evt);
    var confirmation = client.forValueEntity(evt.newEmail()).call(UniqueEmailComponent::confirm);
    var unreserved =
      client
        .forValueEntity(evt.oldEmail())
        .call(UniqueEmailComponent::delete);

    var res = confirmation.execute().thenCompose(__ -> unreserved.execute());
    return effects().asyncReply(res);
  }
}
