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
@Table(name = "judge")
@JsonIgnoreProperties(ignoreUnknown = true)

public class Judge implements Serializable {

	public Judge() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;

	@Getter
	@Column
	@JsonProperty
	private String name;

	@Getter
	@Setter
	@Column
	@JsonIgnore
	private Boolean voted = false;

	public Judge(String name, Boolean voted) {
		this.name = name;
		this.voted = voted;

	}

	@JsonCreator
	public Judge(@JsonProperty("name") String name) {
		this.name = name;
	}
}
