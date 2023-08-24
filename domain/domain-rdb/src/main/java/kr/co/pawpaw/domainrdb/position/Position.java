package kr.co.pawpaw.domainrdb.position;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position {
    private Double latitude;
    private Double longitude;
    private String name;
}
