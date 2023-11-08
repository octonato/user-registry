package user.registry.components.listeners;


import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.registry.Done;
import user.registry.components.entities.UniqueEmailEntity;
import user.registry.components.entities.UserEntity;

@Subscribe.EventSourcedEntity(value = UserEntity.class, ignoreUnknown = true)
public class UserEventsListener extends Action {

  private final ComponentClient client;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private UserEventsListener(ComponentClient client) {
    this.client = client;
  }

  public Effect<Done> onEvent(UserEntity.EmailAssigned evt) {
    logger.info("User got a new email address assigned: {}, confirming new address address", evt);
    var confirmation = client.forValueEntity(evt.newEmail()).call(UniqueEmailEntity::confirm);
    return effects().forward(confirmation);
  }

  public Effect<Done> onEvent(UserEntity.EmailUnassigned evt) {
    logger.info("Old email address unassigned: {}, deleting unique email address record", evt);
    var unreserved = client.forValueEntity(evt.oldEmail()).call(UniqueEmailEntity::delete);
    return effects().forward(unreserved);
  }
}
