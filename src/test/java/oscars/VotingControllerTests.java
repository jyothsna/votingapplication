/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package oscars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import oscars.entity.Judge;
import oscars.entity.Movie;
import oscars.model.VoteSubmissionRequest;
import oscars.repository.JudgesRepository;
import oscars.repository.MoviesRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.JVM)

public class VotingControllerTests {
	private static final Logger logger = LoggerFactory.getLogger(VotingControllerTests.class);
	Boolean shouldPass = true;
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JudgesRepository judgeRepo;

	@Autowired
	private MoviesRepository movieRepo;
	// @MockBean
	// private static VotingService votingService;

	// static Movie mockMovie1 = new Movie(movie1, Thriller);
	// static List<Movie> mockNominatedMovies = new ArrayList<Movie>();
	Boolean setupDone = false;

	@Before
	public void feedTestInput() throws Exception {
		if (!setupDone) {
			// mockNominatedMovies.add(mockMovie1);
			// Mockito.when(votingService.getAllNominatedMovies()).thenReturn(mockNominatedMovies);
			setupDone = true;
		}
	}

	@Test
	public void test0GetMovies() throws Exception {

		String expected = new GsonBuilder().addSerializationExclusionStrategy(this.exclusionStrategy).create().toJson(movieRepo.findAll());
		getMoviesAndVerify(expected, shouldPass);
	}

	/**
	 * 
	 * 7) Admin should be able to add Movies to Nominated Movies List (before
	 * voting starts.
	 * 
	 */
	@Test
	public void test1AdminAddsMovies() throws Exception {
		addAMovieAndVerify(new Movie("movieTest1", "genreTest1"), shouldPass);

	}

	@Test
	public void test2GetJudges() throws Exception {
		
		String expected = new GsonBuilder().addSerializationExclusionStrategy(this.exclusionStrategy).create().toJson(judgeRepo.findAll());
		getJudgesAndVerify(expected, shouldPass);
	}

	/**
	 * Test when voting period has not started
	 * 
	 * @throws Exception
	 */
	@Test
	public void test3BestMovieWinner() throws Exception {
		verifyBestMovieWinner(null, !shouldPass);
	}

	/**
	 * Start voting submit votes 6) An admin cannot add new movies to nominated
	 * list of movies when Voting period has started.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test4StartVotingSubmitVotesNoMoviesCanBeAdded() throws Exception {

		startVoting();

		List<Judge> existingNotVotedJudge = judgeRepo.findJudgesWhoHaveNotVoted();
		verifyVoteSubmission(existingNotVotedJudge.get(0), movieRepo.findAll().get(1), shouldPass);

		addAMovieAndVerify(new Movie("movieTest4", "Comedy"), !shouldPass);

	}

	/**
	 * Judges should be able to find the Best Voted Movie after Voting period
	 * ends
	 */

	@Test
	public void test5GetBestMovieAfterVotingPeriodEnds() throws Exception {

		startVoting();
		List<Judge> existingNotVotedJudge = judgeRepo.findJudgesWhoHaveNotVoted();
		Movie movie = movieRepo.findAll().get(2);
		
		verifyVoteSubmission(existingNotVotedJudge.get(0), movie, shouldPass);
		verifyVoteSubmission(existingNotVotedJudge.get(1), movie, shouldPass);
		verifyVoteSubmission(existingNotVotedJudge.get(2), movie, shouldPass);
		
		logger.info("wait for 61 seconds, so voting period ends");
		Thread.sleep(21000);
		verifyBestMovieWinner(new Movie[] { movie }, shouldPass);
	}

	/**
	 * 4) Admin should be able to extend the voting period 10) An Admin cannot
	 * add new movies to nominated list of movies when Voting period is
	 * extended.
	 */
	@Test
	public void test6AdminExtendsVotingPeriod() throws Exception {

		startVoting();
		List<Judge> existingNotVotedJudge = judgeRepo.findJudgesWhoHaveNotVoted();
		Movie movie = movieRepo.findAll().get(2);

		verifyVoteSubmission(existingNotVotedJudge.get(0), movie, shouldPass);
		logger.info("wait for 61 seconds, so voting period ends");

		Thread.sleep(21000);
		verifyBestMovieWinner(new Movie[] { movie }, shouldPass);

		extendVoting();

		logger.info("Check that we cannot retrieve best movie when voting is in progress");
		verifyBestMovieWinner(new Movie[] { movie }, !shouldPass);

		logger.info("Check that admin cannot add movies when voting is in progress");
		addAMovieAndVerify(new Movie("movieTest9", "genreTest9"), !shouldPass);
	}

	/**
	 * 
	 * 7) A non-existing movie cannot be voted , proper error should be returned
	 * 
	 * 8) A non-existing judge cannot vote, proper error should be returned 9)
	 * Best movie cannot be listed if Voting period is extended
	 */
	@Test
	public void test7voteNonExistingMovieOrJudgeShouldFail() throws Exception {
		// Start Voting
		startVoting();

		List<Judge> existingNotVotedJudge = judgeRepo.findJudgesWhoHaveNotVoted();

		verifyVoteSubmission(existingNotVotedJudge.get(1), new Movie("test12MovieNotInDB", "newGenre"), !shouldPass);
		verifyVoteSubmission(new Judge("test12JudgeNonExistingInDB"), movieRepo.findAll().get(0), !shouldPass);
	}

	/**
	 * 6) Admin should be able to add judges for voting for Best Movie
	 * 
	 */

	@Test
	public void test8AdminAddsJudges() throws Exception {
		Judge judge8 = new Judge("judgeTest8");

		addAJudge(judge8);

	}

	/**
	 * 2) Same Judge cannot vote more than 1 time 3) Same Judge cannot vote more
	 * than 1 time even if voting period is extended 4) A Judge who has not
	 * voted before should be able to vote if Voting period is extended 5) A
	 * Judge who is elected after the voting period is extended should be able
	 * to vote
	 * 
	 */

	@Test
	public void test10TestDuplicateVotesBeforeAfterVotingExtension() throws Exception {
		// Start Voting
		startVoting();

		// vote submisssion verification
		List<Judge> existingNotVotedJudge = judgeRepo.findJudgesWhoHaveNotVoted();
		verifyVoteSubmission(existingNotVotedJudge.get(0), movieRepo.findAll().get(0), shouldPass);

		// Re-vote Submission should fail before Voting extension
		verifyReVote();

		// extend Voting Duraiton
		extendVoting();

		// Re-vote Submission should fail even after Voting extension
		verifyReVote();

		// existing judge who hasn't yet voted: vote submission should pass
		// after Voting extension
		verifyVoteSubmission(existingNotVotedJudge.get(1), movieRepo.findAll().get(1), shouldPass);

		// newly added judge's vote submission should pass after Voting
		// extension
		Judge judgeTest11 = new Judge("judgeTest11");
		addAJudge(judgeTest11);
		verifyVoteSubmission(judgeTest11, movieRepo.findAll().get(1), shouldPass);

	}

	/**
	 * 5) Admin should not be able to clear the movies and Judges when voting is
	 * in progress
	 * 
	 * Admin should able to clear the movies and Judges when voting is not in
	 * progress
	 */
	@Test
	public void test11AdminClearsMoviesAndJudge() throws Exception {

		startVoting();
		resetVotingAndVerify(!shouldPass);
		String expected = "[]";
		getMoviesAndVerify(expected, !shouldPass);

		getJudgesAndVerify(expected, !shouldPass);

		logger.info("wait for 61 seconds, so voting period ends");
		Thread.sleep(61000);

		resetVotingAndVerify(shouldPass);
		getMoviesAndVerify(expected, shouldPass);

		getJudgesAndVerify(expected, shouldPass);

	}

	private void verifyReVote() throws Exception {
		VoteSubmissionRequest testVoteRequest2 = new VoteSubmissionRequest("movie2", "judge1");

		RequestBuilder voteSumbitRequestBuilder = MockMvcRequestBuilders.post("/vote")
				.accept(MediaType.APPLICATION_JSON).content(new Gson().toJson(testVoteRequest2))
				.contentType(MediaType.APPLICATION_JSON);
		logger.info("sending vote submit request");
		this.mockMvc.perform(voteSumbitRequestBuilder);
		logger.info("sent vote submit request");

		// re-vote
		logger.info("sending vote submit request again");
		MvcResult revoteSubmissionresult = this.mockMvc.perform(voteSumbitRequestBuilder).andDo(print()).andReturn();
		logger.info("sent vote submit request again");

		// verify status code
		assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), revoteSubmissionresult.getResponse().getStatus());
		String revoteSubmissionExpectedMessage = "re-voting is not allowed or judge/movie not found";
		assertEquals("Response for vote submit request is not as expected", revoteSubmissionExpectedMessage,
				revoteSubmissionresult.getResponse().getContentAsString());
	}

	private void verifyBestMovieWinner(Movie[] movie, Boolean pass) throws Exception {
		RequestBuilder bestMovieWinnerRequestBuilder = MockMvcRequestBuilders.get("/awardGoesTo")
				.accept(MediaType.APPLICATION_JSON);
		logger.info("Sending best movie winner Request");

		MvcResult bestMovieresult = this.mockMvc.perform(bestMovieWinnerRequestBuilder).andDo(print()).andReturn();
		logger.info("Sent best movie winner request");

		if (pass) {
			String expected = new GsonBuilder().addSerializationExclusionStrategy(this.exclusionStrategy).create().toJson(movie);
			// verify response
			JSONAssert.assertEquals(expected, bestMovieresult.getResponse().getContentAsString(), false);
			// verify status code
			assertEquals(HttpStatus.OK.value(), bestMovieresult.getResponse().getStatus());
		} else {
			// verify status code
			assertEquals(HttpStatus.LOCKED.value(), bestMovieresult.getResponse().getStatus());
			assertTrue("best Movie winner response is not empty",
					bestMovieresult.getResponse().getContentAsString().isEmpty());

		}

	}

	private void verifyVoteSubmission(Judge judge, Movie movie, Boolean pass) throws Exception {
		VoteSubmissionRequest testVoteRequest2 = new VoteSubmissionRequest(movie.getTitle(), judge.getName());
		logger.info("sending vote submit request");

		RequestBuilder voteSumbitRequestBuilder = MockMvcRequestBuilders.post("/vote")
				.accept(MediaType.APPLICATION_JSON).content(new Gson().toJson(testVoteRequest2))
				.contentType(MediaType.APPLICATION_JSON);
		MvcResult revoteSubmissionresult = this.mockMvc.perform(voteSumbitRequestBuilder).andDo(print()).andReturn();
		logger.info("sent vote submit request");

		if (pass) {
			// verify status code
			assertEquals(HttpStatus.OK.value(), revoteSubmissionresult.getResponse().getStatus());
			String voteSubmissionExpectedMessage = "Vote submission success";
			assertEquals("Response for vote submit request is not as expected", voteSubmissionExpectedMessage,
					revoteSubmissionresult.getResponse().getContentAsString());
		} else {
			assertNotEquals(HttpStatus.OK.value(), revoteSubmissionresult.getResponse().getStatus());
			String voteSubmissionExpectedMessage = "Vote submission success";
			assertNotEquals("Response for vote submit request is not as expected", voteSubmissionExpectedMessage,
					revoteSubmissionresult.getResponse().getContentAsString());
		}
	}

	private void extendVoting() throws Exception {
		logger.info("sending restart voting request ");

		RequestBuilder restartVotingrequestBuilder = MockMvcRequestBuilders.post("/restartVoting/1")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);

		this.mockMvc.perform(restartVotingrequestBuilder).andDo(print()).andReturn();
		logger.info("sent restart voting request ");

	}

	private void addAJudge(Judge judge) throws Exception {
		RequestBuilder addJudgeRequestBuilder = MockMvcRequestBuilders.post("/judge").content(new Gson().toJson(judge))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		logger.info("sending submit a judge request ");

		MvcResult addJudgeresult = this.mockMvc.perform(addJudgeRequestBuilder).andDo(print()).andReturn();
		logger.info("sent submit a judge request ");

		// verify status code
		// verify status code
		assertEquals(HttpStatus.OK.value(), addJudgeresult.getResponse().getStatus());
		String expectedMessage = "true";
		// verify response
		assertEquals("Response for adding Judges request is not as expected", expectedMessage,
				addJudgeresult.getResponse().getContentAsString());
		// verify DB
		assertEquals("new judge not found in repo", judge.getName(), judgeRepo.findByName(judge.getName()).getName());

	}

	private void addAMovieAndVerify(Movie movie, Boolean pass) throws Exception {
		RequestBuilder addMovieRequestBuilder = MockMvcRequestBuilders.post("/movie").content(new Gson().toJson(movie))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);

		logger.info("sending submit a movie request ");

		MvcResult addMoviesresult = this.mockMvc.perform(addMovieRequestBuilder).andDo(print()).andReturn();
		logger.info("sent submit a movie request ");

		if (pass) {
			// verify status code
			// verify status code
			assertEquals(HttpStatus.OK.value(), addMoviesresult.getResponse().getStatus());
			String expectedMessage = "success adding to nominated movies list";
			assertEquals("Response for adding movies request is not as expected", expectedMessage,
					addMoviesresult.getResponse().getContentAsString());
			// Check DB
			assertEquals("new movie not found in repo", movie.getTitle(),
					movieRepo.findByTitle(movie.getTitle()).getTitle());
		} else {
			// verify status code
			assertEquals(HttpStatus.LOCKED.value(), addMoviesresult.getResponse().getStatus());
			String expectedMessage = "Voting is in Progess";
			assertEquals("Response for adding movies request is not as expected", expectedMessage,
					addMoviesresult.getResponse().getContentAsString());
			// Check DB
			assertNull("new movie found in repo which is not expected", movieRepo.findByTitle(movie.getTitle()));
		}

	}

	private void startVoting() throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/startVoting").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		logger.info("sending start voting request ");

		MvcResult startVotingresult = this.mockMvc.perform(requestBuilder).andDo(print()).andReturn();
		logger.info("sent start voting request ");
		// verify status code
		assertEquals(HttpStatus.OK.value(), startVotingresult.getResponse().getStatus());
		String expectedMessage = "Voting Period has begun now, default duration is 1 min";
		assertEquals("Response for startvoting request is not as expected", expectedMessage,
				startVotingresult.getResponse().getContentAsString());
	}

	private void getMoviesAndVerify(String expected, Boolean pass) throws Exception {
		RequestBuilder moviesRequestBuilder = MockMvcRequestBuilders.get("/movies").accept(MediaType.APPLICATION_JSON);

		MvcResult result = this.mockMvc.perform(moviesRequestBuilder).andDo(print()).andReturn();
		// verify status code
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		if (pass) {
			// verify response
			JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
		} else {
			// verify response
			JSONAssert.assertNotEquals(expected, result.getResponse().getContentAsString(), false);
		}
	}

	private void getJudgesAndVerify(String expected, Boolean pass) throws Exception {
		RequestBuilder judgesRequestBuilder = MockMvcRequestBuilders.get("/judges").accept(MediaType.APPLICATION_JSON);

		MvcResult result = this.mockMvc.perform(judgesRequestBuilder).andDo(print()).andReturn();
		// verify status code
		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		if (pass) {
			// verify response
			JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
		} else {
			// verify response
			JSONAssert.assertNotEquals(expected, result.getResponse().getContentAsString(), false);
		}
	}

	private void resetVotingAndVerify(Boolean pass) throws Exception {
		logger.info("sending reset voting request");
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/resetVoting")
				.accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON);

		MvcResult resetVotingresult = this.mockMvc.perform(requestBuilder).andDo(print()).andReturn();
		logger.info("sendtreset voting request");

		if (pass) {
			// verify status code
			assertEquals(HttpStatus.OK.value(), resetVotingresult.getResponse().getStatus());
			String expectedMessage = "true";
			assertEquals("Response for reset Voting request is not as expected", expectedMessage,
					resetVotingresult.getResponse().getContentAsString());
		} else {
			// verify status code
			assertEquals(HttpStatus.LOCKED.value(), resetVotingresult.getResponse().getStatus());
			String expectedMessage = "false";
			assertEquals("Response for reset Voting request is not as expected", expectedMessage,
					resetVotingresult.getResponse().getContentAsString());
		}

	}
	
	private ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			if (f.getName().toLowerCase().contains("id") || f.getName().toLowerCase().contains("votes") || f.getName().toLowerCase().contains("voted") )
				return true;
			else
				return false;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	// @Test
	// public void paramGreetingShouldReturnTailoredMessage() throws Exception {
	//
	// this.mockMvc.perform(get(/greeting).param(name, Spring Community))
	// .andDo(print()).andExpect(status().isOk())
	// .andExpect(jsonPath($.content).value(Hello, Spring Community!));
	// }

}

//
// /**
// * 9) Unsupported Method calls should be handled gracefully
// */
//
// @DataProvider
// public static Object[][] allGetAPIs() {
//	// @formatter:off
//    return new Object[][] {
//            { "/movies", 0, 0 },
//            { "/judges", 1, 2 },
//            { "/awardGoesTo", 1, 2 }
//           
//            
//            /* ... */
//    };
//    // @formatter:on
// }
//
// @DataProvider
// public static Object[][] allPostAPIs() {
//	// @formatter:off
//    return new Object[][] {
//            { "/resetVoting", 1, 2 },
//            { "/restartVoting", 1, 2 },
//            { "/vote", 1, 2 },
//            { "/resetVoting", 1, 2 },
//            { "/startVoting", 1, 2 }
//
//            
//            /* ... */
//    };
//    // @formatter:on
// }
//
//
// @Test
// @UseDataProvider("allPostAPIs")
// public void test9TestAllAPIs(String apiPath, int b, int expected) throws
// Exception {
// RequestBuilder requestBuilder = MockMvcRequestBuilders.get(apiPath)
// .accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON);
//
// MvcResult apiResult =
// this.mockMvc.perform(requestBuilder).andDo(print()).andReturn();
// //verify status code
// assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(),
// apiResult.getResponse().getStatus());
//
// }
//
// @Test
// @UseDataProvider("allGetAPIs")
// public void test10TestAllGetAPIs(String apiPath, int b, int expected)
// throws Exception {
// RequestBuilder requestBuilder =
// MockMvcRequestBuilders.post(apiPath).content("")
// .accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON);
//
// MvcResult apiResult =
// this.mockMvc.perform(requestBuilder).andDo(print()).andReturn();
// //verify status code
// assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(),
// apiResult.getResponse().getStatus());
// }
