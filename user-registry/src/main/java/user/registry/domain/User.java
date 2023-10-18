package user.registry.domain;


import io.vavr.control.Either;

public class User {

  public final String email;
  public final String name;

  public User(String name, String email) {
    this.name = name;
    this.email = email;
  }

  // commands
  public record Create(String name, String email) {}
  public record ChangeEmail(String email) {}

  // events
  public sealed interface Event {}
  public record Created(String name, String email) implements Event {}
  public record EmailChanged(String email) implements Event {}

  static public Event onCommand(Create cmd) {
    return new Created(cmd.name(), cmd.email());
  }

  static public  User onEvent(Created evt) {
    return new User(evt.name(), evt.email());
  }

  public Either<String, Event> onCommand(ChangeEmail cmd) {
     if(cmd.email().equals(email))
       return Either.left("Email is the same as the current one");
     else
       return Either.right(new EmailChanged(cmd.email()));
  }

  public User onEvent(EmailChanged evt) {
    return new User(name, evt.email());
  }
}
