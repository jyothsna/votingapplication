package oscars.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import oscars.entity.Judge;
import oscars.entity.Movie;
import oscars.model.VoteSubmissionRequest;
import oscars.service.VotingService;

@RestController
public class VotingController {
	private static final Logger logger = LoggerFactory.getLogger(VotingController.class);

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@Autowired
	private VotingService votingService;


	@RequestMapping("/movies")
	public  ResponseEntity<List<Movie>> getNominatedMovies() {
		return  new ResponseEntity<List<Movie>>(this.votingService.getAllNominatedMovies(), HttpStatus.OK);
	}

	@RequestMapping(value = "/vote", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> submitVote(@RequestBody(required = true) VoteSubmissionRequest request) {

		HttpStatus returnStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		if (this.votingService.hasVotingPeriodEnded() || !this.votingService.getVotingStarted() ) {
			returnStatus = HttpStatus.LOCKED;
			logger.error("Voting period ended ");

			return new ResponseEntity<String>("Voting is not in progress", returnStatus);
		}
		try {
			returnStatus = this.votingService.processVote(request);

		} catch (Exception e) {
			logger.error("Failed to process vote: " + e);
			e.printStackTrace();
			return new ResponseEntity<String>("re-voting is not allowed or judge/movie not found", HttpStatus.NOT_ACCEPTABLE);

		}

		return new ResponseEntity<String>("Vote submission success", returnStatus);
	}

	@RequestMapping(value = "/awardGoesTo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull
	public ResponseEntity<List<Movie>> bestMovieWinner() {
		if (!this.votingService.hasVotingPeriodEnded() || this.votingService.getVotingStarted() ) {
			logger.error("Voting Period has not begun or ended to reveal the best voted Movie");
			return new ResponseEntity<List<Movie>>(HttpStatus.LOCKED);
		}
		return new ResponseEntity<List<Movie>>(this.votingService.getAllBestVotedMovies(), HttpStatus.OK);

	}

	// TODO restrict to Admin
	@RequestMapping(value = "/movies", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull
	public ResponseEntity<String> submitNominatedMovies(@RequestBody(required = true) Movie[] request) {

		HttpStatus returnStatus = HttpStatus.BAD_REQUEST;
		String message = "Voting is in Progess";
		if (this.votingService.hasVotingPeriodEnded() || !this.votingService.getVotingStarted()) {
			message = "success adding to nominated movies list";
			returnStatus = this.votingService.saveMovies(request);
		}
		return new ResponseEntity<String>(message, returnStatus);

	}
	
	// TODO restrict to Admin
		@RequestMapping(value = "/movie", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
		@NotNull
		public ResponseEntity<String> submitNominatedMovie(@RequestBody(required = true) Movie request) {

			HttpStatus returnStatus = HttpStatus.LOCKED;
			String message = "Voting is in Progess";
			if (this.votingService.hasVotingPeriodEnded() || !this.votingService.getVotingStarted()) {
				message = "success adding to nominated movies list";
				returnStatus = this.votingService.saveMovie(request);
			}
			return new ResponseEntity<String>(message, returnStatus);

		}

	// TODO restrict to Admin
	@RequestMapping(value = "/judges", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull
	public ResponseEntity<Boolean> submitJudges(@RequestBody(required = true) Judge[] request) {
		return new ResponseEntity<Boolean>(Boolean.TRUE, this.votingService.saveJudges(request));
	}
	

	// TODO restrict to Admin
	@RequestMapping(value = "/judge", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull
	public ResponseEntity<Boolean> submitJudge(@RequestBody(required = true) Judge request) {
		return new ResponseEntity<Boolean>(Boolean.TRUE, this.votingService.saveJudge(request));
	}

	// TODO restrict to Admin
	@RequestMapping("/judges")
	public List<Judge> getJudges() {
		return this.votingService.getAllJudges();
	}

	// TODO restrict to Admin
	@RequestMapping(value = "/resetVoting", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> resetVoting() {
		HttpStatus returnStatus = HttpStatus.LOCKED;
		Boolean success = false;
		if (this.votingService.hasVotingPeriodEnded() || !this.votingService.getVotingStarted()) {
			returnStatus = this.votingService.resetVotingMoviesAndJudges();
			success = true;
		}
		return new ResponseEntity<Boolean>(success,returnStatus);
	}

	// TODO restrict to Admin
	// Reset timer
	@RequestMapping(value = "/restartVoting/{durationInMinutes}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> restartVoting(@PathVariable("durationInMinutes") Integer durationInMinutes) {
		 return new ResponseEntity<Boolean>(true, this.votingService.resetVotingDuration(durationInMinutes));
	}
	
	// TODO restrict to Admin
	// Reset timer
	@RequestMapping(value = "/startVoting", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> startVoting() {
		logger.info("Voting Period has begun now, default duration is 1 min");

		HttpStatus returnStatus = this.votingService.resetVotingDuration();
		this.votingService.setVotingStarted(true);
		return new ResponseEntity<String>("Voting Period has begun now, default duration is 1 min", returnStatus);
	}

	// TODO restrict to Admin
	// Check voting period status
	@RequestMapping(value = "/isVotinginProgress", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> checkVotingStatus() {
		logger.info("Checking if Voting is in progress");
		if (this.votingService.hasVotingPeriodEnded() || !this.votingService.getVotingStarted() ) {
			return new ResponseEntity<Boolean>(false , HttpStatus.OK);

		}
		return new ResponseEntity<Boolean>(true , HttpStatus.OK);
	}

}
