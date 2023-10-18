package user.registry.components;

import org.junit.jupiter.api.Test;

import kalix.javasdk.testkit.EventSourcedTestKit;
import user.registry.domain.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserEntityComponentTest {

  @Test
  public void testCreationAndUpdate() {
    var userTestKit = EventSourcedTestKit.of(__ -> new UserEntityComponent());

    var creationRes = userTestKit.call(userService -> userService.createUser(new User.Create("John", "john@acme.com")));

    var created = creationRes.getNextEventOfType(User.Created.class);
    assertEquals("John", created.name());
    assertEquals("john@acme.com", created.email());

    var updateRes = userTestKit.call(userService -> userService.changeEmail(new User.ChangeEmail("john.doe@acme.com")));
    var emailChanged = updateRes.getNextEventOfType(User.EmailChanged.class);
    assertEquals("john.doe@acme.com", emailChanged.email());
  }

  @Test
  public void updateNonExistentUser() {
    var userTestKit = EventSourcedTestKit.of(__ -> new UserEntityComponent());

    var updateRes = userTestKit.call(userService -> userService.changeEmail(new User.ChangeEmail("john.doe@acme.com")));
    assertTrue(updateRes.isError());
    assertEquals("User not found", updateRes.getError());
  }
}
