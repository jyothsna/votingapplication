package oscars.entity;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class VotingDuration implements Serializable {
	
	public VotingDuration(){
		
	}
	
    @Getter
    @JsonProperty
    
    private  Duration votingDuration = Duration.ofMinutes(1);
    
    public void setVotingDuration(Duration votingDuration) {
		this.votingDuration = votingDuration;
		this.start = Instant.now();
	}
    
    public void resetVotingDuration() {
		this.start = Instant.now();
	}

	@Getter
    private Instant start = Instant.now();

    @JsonCreator
    public VotingDuration( @JsonProperty("votingDuration") Duration votingDuration) {
    	this.votingDuration = votingDuration;
    	this.start = Instant.now();

    }
    
}
