package user.registry.components.actions;


import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.registry.Done;
import user.registry.components.entities.UniqueEmailComponent;

import java.time.Duration;

@Subscribe.ValueEntity(UniqueEmailComponent.class)
public class UniqueEmailListener extends Action {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final ComponentClient client;

  public UniqueEmailListener(ComponentClient client) {
    this.client = client;
  }

  public Effect<Done> onChange(UniqueEmailComponent.UniqueEmail email) {

    logger.info("Received update for address '{}'", email);
    var timerId = "timer-" + email.address();

    if (email.isConfirmed()) {
      logger.info("Email is already confirmed, deleting timer (if exists) '{}'", timerId);
      var cancellation = timers().cancel(timerId);
      return effects().asyncReply(cancellation.thenApply(__ -> Done.done()));

    } else if (email.isReserved()) {
      Duration delay = Duration.ofSeconds(10);
      logger.info("Email is not confirmed, scheduling timer '{}' to fire in '{}'", timerId, delay);
      var callToDelete =
        client
          .forValueEntity(email.address())
          .call(UniqueEmailComponent::delete);

      var timer = timers().startSingleTimer(
        timerId,
        delay,
        callToDelete);

      return effects().asyncReply(timer.thenApply(__ -> Done.done()));
    } else {
      // Email is not reserved, so we don't need to do anything
      return effects().reply(Done.done());
    }
  }

}
