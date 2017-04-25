package com.example.converter;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.BookRepository;
import com.example.dto.BookDto;
import com.example.entity.Book;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class BookMapper {

	public abstract Book toDomain(BookDto dto);
	
	@Autowired
	private BookRepository repo;
	
	@ObjectFactory
	public Book entityFactory(@TargetType Class<Book> type, BookDto dto) {
		if (dto == null || 
			dto.getId() == null) {
			return new Book();
		}

		return repo.findOne(dto.getId());
	}	
}
