
package oscars.repository;

import java.util.List;

import javax.transaction.Transactional;

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
	
	@Transactional
	@Modifying
	@Query("Update  Judge  set voted = false")
    void clearVoted();
	

}
