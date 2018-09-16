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

import oscars.entity.Judge;
import oscars.entity.Movie;


/**
 * This is the repository for movies
 */
public interface JudgesRepository extends
		JpaRepository<Judge, String> {

	/**
	 * This method gets the Judge object based upon a Name
	 * 
	 * @param name
	 *            name of the Judge
	 * @return Judge object
	 */
	Judge findByName(String name);
	
	@Query("FROM Judge  WHERE voted = false")
    List<Judge> findJudgesWhoHaveNotVoted();
	
	@Modifying
	@Query("Update  Judge  set voted = false")
    List<Movie> clearVoted();
	

}