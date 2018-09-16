/**
  * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
package oscars.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import oscars.entity.Movie;


/**
 * This is the repository for movies
 */
public interface MoviesRepository extends
		JpaRepository<Movie, String> {

	/**
	 * This method gets the Movie object based upon a Title
	 * 
	 * @param title
	 *            title of the movie
	 * @return Movie object
	 */
	Movie findByTitle(String title);
	
	@Query("FROM Movie  WHERE votes = (SELECT MAX(votes) FROM Movie)")
    List<Movie> findMaxVoted();
	
	@Transactional
	@Modifying
	@Query("Update  Movie  set votes = 0")
    void clearVotes();
}
	
