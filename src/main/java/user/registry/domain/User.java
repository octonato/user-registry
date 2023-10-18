package user.registry.domain;


import io.vavr.control.Either;

public class User {

  public final String email;
  public final String name;
  public final String country;

  public User(String name, String country, String email) {
    this.name = name;
    this.email = email;
    this.country = country;
  }

  // commands
  public record Create(String name, String country, String email) {}
  public record ChangeEmail(String newEmail) {}
  public record ChangeCountry(String newCountry) {}

  // events
  public sealed interface Event {}
  public record UserWasCreated(String name, String country, String email) implements Event {}
  public record UsersEmailChanged(String oldEmail, String newEmail) implements Event {}
  public record UsersCountryChanged(String newCountry) implements Event {}

  static public Event onCommand(Create cmd) {
    return new UserWasCreated(cmd.name(), cmd.country(), cmd.email());
  }

  static public  User onEvent(UserWasCreated evt) {
    return new User(evt.name(), evt.country(), evt.email());
  }

  public Either<String, Event> onCommand(ChangeCountry cmd) {
    if(cmd.newCountry().equals(country))
      return Either.left("Country is the same as the current one");
    else
      return Either.right(new UsersCountryChanged(cmd.newCountry()));
  }

  public User onEvent(UsersCountryChanged evt) {
    return new User(name, evt.newCountry(), email);
  }

  public Either<String, Event> onCommand(ChangeEmail cmd) {
     if(cmd.newEmail().equals(email))
       return Either.left("Email is the same as the current one");
     else
       return Either.right(new UsersEmailChanged(email, cmd.newEmail()));
  }

  public User onEvent(UsersEmailChanged evt) {
    return new User(name, country,  evt.newEmail());
  }
}
