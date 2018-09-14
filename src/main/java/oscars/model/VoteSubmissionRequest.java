package oscars.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VoteSubmissionRequest {
	@Getter
	String movieTitle;
	
	@Getter
	String judge;
	
	@JsonCreator
	public VoteSubmissionRequest(@JsonProperty("movieTitle") String movieTitle, @JsonProperty("judge") String judge){
		this.judge=judge;
		this.movieTitle = movieTitle;
	}

}
