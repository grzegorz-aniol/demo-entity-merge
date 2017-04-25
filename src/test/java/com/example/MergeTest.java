package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.converter.BookMapper;
import com.example.dto.BookDto;
import com.example.entity.Book;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@Transactional(isolation=Isolation.READ_UNCOMMITTED)
@Rollback(value=false)
public class MergeTest {

	private static final String NEW_DESCRIPTION = "Here is new description";

	private static final String DESCRIPTION = "This is very interesting book!";

	private static final String TITLE = "Title of the book";

	@Autowired
	BookRepository repo;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	BookMapper mapper;
	
	@Test
	public void testJPARepositorySave() {
		Long id = inTransaction(()->{
			Book b = Book.builder()
					.title(TITLE)
					.description(DESCRIPTION)
					.pages(255)
					.build();
			b = repo.save(b);
			return b.getId();
		});
		
		Book savedBook = inTransaction(() -> {	
			// change description only. don't touch title. 
			Book bookUpdate = Book.builder()
				.id(id)
				.description(NEW_DESCRIPTION)
				.build();
			return repo.save(bookUpdate); // entity manager merge
		});
		
		assertThat(savedBook.getId())
			.as("Book ID should be preserved!")
			.isEqualTo(id);
		
		// read book and check changed
		Book lastChange = repo.findOne(id);
		
		assertThat(lastChange).isNotNull();
		assertThat(lastChange.getTitle()).isNotNull().isEqualTo(TITLE);
		assertThat(lastChange.getDescription()).isNotNull().isEqualTo(NEW_DESCRIPTION);
	}
	
	
	@Test
	public void testDtoUpdate() {
		Long id = inTransaction(()->{
			Book b = Book.builder()
					.title(TITLE)
					.description(DESCRIPTION)
					.pages(255)
					.build();
			b = repo.save(b);
			return b.getId();
		});
		
		Book savedBook = inTransaction(() -> {	
			// change description only. don't touch title. 
			BookDto dto = BookDto.builder()
					.id(id)
					.description(NEW_DESCRIPTION)
					.build();
			
			Book bookUpdate = mapper.toDomain(dto);
			
			return repo.save(bookUpdate); // entity manager merge
		});
		
		assertThat(savedBook.getId())
			.as("Book ID should be preserved!")
			.isEqualTo(id);
		
		// read book and check changed
		Book lastChange = repo.findOne(id);
		
		assertThat(lastChange).isNotNull();
		assertThat(lastChange.getTitle()).isNotNull().isEqualTo(TITLE);
		assertThat(lastChange.getDescription()).isNotNull().isEqualTo(NEW_DESCRIPTION);
	}	
	
	@Test
	public void testEntityManagerMerge() {
		Long id = inTransaction(()->{
			Book b = Book.builder()
					.title(TITLE)
					.description(DESCRIPTION)
					.pages(255)					
					.build();
			em.persist(b);
			return b.getId();
		});
		
		inTransaction(() -> {			
			Book bookUpdate = Book.builder()
				.id(id)
				.description(NEW_DESCRIPTION)
				.build();
			
			bookUpdate = em.merge(bookUpdate);
			
			return bookUpdate;
		});
		
		// read book and check changed
		Book lastChange = em.find(Book.class, id);
		
		assertThat(lastChange).isNotNull();
		assertThat(lastChange.getTitle()).isNotNull();
	}	
    
	@Test
	public void testEntityManagerMergeDetached() {
		Long id = inTransaction(()->{
			Book b = Book.builder()
					.title(TITLE)
					.description(DESCRIPTION)
					.pages(255)					
					.build();
			em.persist(b);
			return b.getId();
		});
		
		inTransaction(() -> {			
			// load an entity to L1
			Book b = em.find(Book.class, id);
			
			Book bookUpdate = Book.builder()
				.id(id)
				.description(NEW_DESCRIPTION)
				.build();
			
			bookUpdate = em.merge(bookUpdate);
			
			return bookUpdate;
		});
		
		// read book and check changed
		Book lastChange = em.find(Book.class, id);
		
		assertThat(lastChange).isNotNull();
		assertThat(lastChange.getTitle()).isNotNull();
	}	
    	
	
    /** Execute an code defined by <code>Runnable</code> and commit results.
     * 
     * Function commit data after calling <code>Runnable</code> 
     * and starts a new transaction on completion. It is used in integration test to be sure that
     * data changes are committed and visible by other transactions or threads.  
     *   
     * @param runnable
     */
    protected void inTransaction(Runnable runnable) {
		runnable.run();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();    	
    }

    /** Execute an code defined by <code>Callable</code> and commit results.
     * 
     * Function commit data after calling <code>Callable</code> 
     * and starts a new transaction on completion. It is used in integration test to be sure that
     * data changes are committed and visible by other transactions or threads.  
     *   
     * @param runnable
     */
	protected <V> V inTransaction(Callable<V> callable) {
		V value = null;
		try {
			value = callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
		return value; 
	}		
}
