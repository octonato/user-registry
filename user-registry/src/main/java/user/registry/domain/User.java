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
  public record ChangeEmail(String email) {}
  public record ChangeCountry(String country) {}

  // events
  public sealed interface Event {}
  public record UserWasCreated(String name, String country, String email) implements Event {}
  public record UsersEmailChanged(String newEmail) implements Event {}
  public record UsersCountryChanged(String newCountry) implements Event {}

  static public Event onCommand(Create cmd) {
    return new UserWasCreated(cmd.name(), cmd.country(), cmd.email());
  }

  static public  User onEvent(UserWasCreated evt) {
    return new User(evt.name(), evt.country(), evt.email());
  }

  public Either<String, Event> onCommand(ChangeCountry cmd) {
    if(cmd.country().equals(country))
      return Either.left("Country is the same as the current one");
    else
      return Either.right(new UsersCountryChanged(cmd.country()));
  }

  public User onEvent(UsersCountryChanged evt) {
    return new User(name, evt.newCountry(), email);
  }

  public Either<String, Event> onCommand(ChangeEmail cmd) {
     if(cmd.email().equals(email))
       return Either.left("Email is the same as the current one");
     else
       return Either.right(new UsersEmailChanged(cmd.email()));
  }

  public User onEvent(UsersEmailChanged evt) {
    return new User(name, country,  evt.newEmail());
  }
}
