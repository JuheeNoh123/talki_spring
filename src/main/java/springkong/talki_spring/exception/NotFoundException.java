package springkong.talki_spring.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String target){
        super(target);
    }
}