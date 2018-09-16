package oscars.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie implements Serializable {

	public Movie() {

	}

	public Movie(String title) {
		this.title = title;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;

	// /**
	// *
	// */
	// @Id
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	// private static final long serialVersionUID = 1L;

	@Getter
	@Column
	@JsonProperty
	private String title;

	@Getter
	@Column
	@JsonProperty
	private String genre;

	@Getter
	@Setter
	@Column
	@JsonIgnore
	private Integer votes = 0;

	@JsonCreator
	public Movie(@JsonProperty("title") String title, @JsonProperty("genre") String genre) {
		this.title = title;
		this.genre = genre;

	}
}
