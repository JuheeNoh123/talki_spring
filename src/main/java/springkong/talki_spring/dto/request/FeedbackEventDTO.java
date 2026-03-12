package springkong.talki_spring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEventDTO {
    private String type;
    private double start;
    private double end;
    private double duration;
}