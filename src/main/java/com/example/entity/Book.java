package com.example.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.springframework.data.domain.Persistable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@DynamicUpdate
@SelectBeforeUpdate
@Table(name="book")
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Book implements Persistable<Long> {

	private static final long serialVersionUID = -5129419623545953595L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
//	
//	@Version
//	private Long version;
	
	private String title; 
	
	private int pages;
	
	private String description;

	@Override
	public boolean isNew() {
		return id==null;
	}
		
}
