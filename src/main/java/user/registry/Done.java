package user.registry;

public record Done() {


  public static Done done() {
    return new Done();
  }

}
