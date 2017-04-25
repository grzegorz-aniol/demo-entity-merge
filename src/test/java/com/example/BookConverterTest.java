package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.example.converter.BookMapper;
import com.example.dto.BookDto;
import com.example.entity.Book;

public class BookConverterTest {

	BookMapper mapper = Mappers.getMapper(BookMapper.class);
	
	@Test
	public void convertDtoToDomainTest() {
		BookDto dto = BookDto.builder()
			.id(123L)
			.title("title")
			.description("description")
			.pages(123)
			.build();
		
		Book entity = mapper.toDomain(dto);
		
		assertThat(entity).isNotNull();
		
	}
	
	
	
}
