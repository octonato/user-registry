package user.registry;

public interface Response {

  record Done() implements Response {}

  static Response done() {
    return new Done();
  }

}
