package springkong.talki_spring.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String target){
        super("존재하지 않는 "+target+ " 입니다.");
    }
}