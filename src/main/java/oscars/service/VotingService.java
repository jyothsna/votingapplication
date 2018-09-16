package oscars.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import oscars.entity.Judge;
import oscars.entity.Movie;
import oscars.entity.VotingDuration;
import oscars.model.VoteSubmissionRequest;
import oscars.repository.JudgesRepository;
import oscars.repository.MoviesRepository;

@Component

public class VotingService {

	@Autowired
	private MoviesRepository movieRepo;

	@Autowired
	private JudgesRepository judgeRepo;

	@Autowired
	private VotingDuration votingDuration;

	@Getter
	@Setter
	private Boolean votingStarted = false;

	public HttpStatus processVote(VoteSubmissionRequest request) throws Exception {
		Judge judge = judgeRepo.findByName(request.getJudge());
		if (judge.getVoted()) {
			throw new Exception("Judge " + request.getJudge() + " already voted. Re-voting is not supported/allowed");
		}
		Movie movie = movieRepo.findByTitle(request.getMovieTitle());
		movie.setVotes(movie.getVotes() + 1);
		movieRepo.saveAndFlush(movie);
		judge.setVoted(true);
		judgeRepo.saveAndFlush(judge);
		return HttpStatus.OK;
	}

	public HttpStatus saveMovies(Movie[] request) {
		for (Movie movie : request) {
			movieRepo.save(movie);
		}

		return HttpStatus.OK;
	}

	public HttpStatus saveMovie(Movie request) {
		movieRepo.save(request);
		return HttpStatus.OK;
	}

	public List<Movie> getAllNominatedMovies() {
		return movieRepo.findAll();
	}

	public HttpStatus saveJudges(Judge[] request) {
		for (Judge judge : request) {
			judgeRepo.save(judge);
		}

		return HttpStatus.OK;
	}

	public HttpStatus saveJudge(Judge request) {
		judgeRepo.save(request);
		return HttpStatus.OK;
	}

	public List<Judge> getAllJudges() {
		// TODO Auto-generated method stub
		return judgeRepo.findAll();
	}

	public HttpStatus resetVotingDuration(Integer durationInMinutes) {
		this.votingDuration.setVotingDuration(Duration.ofMinutes(durationInMinutes));
		this.votingStarted = true;
		return HttpStatus.OK;
	}

	public HttpStatus resetVotingDuration() {
		this.votingDuration.resetVotingDuration();
		this.votingStarted = true;
		return HttpStatus.OK;
	}

	public @NotNull List<Movie> getAllBestVotedMovies() {
		return movieRepo.findMaxVoted();
	}

	public HttpStatus resetVotingMoviesAndJudges() {
		judgeRepo.deleteAll();
		movieRepo.deleteAll();
		return HttpStatus.OK;

	}

	public Boolean hasVotingPeriodEnded() {
		Instant now = Instant.now();
		Instant expiredInstant = votingDuration.getStart().plus(votingDuration.getVotingDuration());
		Boolean hasExpired = expiredInstant.isBefore(now);
// 		if (hasExpired) {
// 			this.votingStarted = false;
// 		}
		return hasExpired;
	}

	public HttpStatus startVoting() {
		HttpStatus returnStatus = this.resetVotingDuration();
		this.setVotingStarted(true);
		movieRepo.clearVotes();
		judgeRepo.clearVoted();
		return returnStatus;
	}
}
