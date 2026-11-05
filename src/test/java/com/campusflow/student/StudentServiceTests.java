package com.campusflow.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTests {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getAllStudentsReturnsRepositoryResults() {
        List<Student> students = List.of(
                new Student("Ada Lovelace", "ada@example.com", "Math")
        );
        when(studentRepository.findAll()).thenReturn(students);

        assertSame(students, studentService.getAllStudents());
    }

    @Test
    void getStudentByIdReturnsStudentWhenFound() {
        Student student = new Student("Ada Lovelace", "ada@example.com", "Math");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertSame(student, studentService.getStudentById(1L));
    }

    @Test
    void getStudentByIdThrowsWhenMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class,
                () -> studentService.getStudentById(1L));
    }

    @Test
    void createStudentSavesStudentWhenEmailIsAvailable() {
        Student student = new Student("Ada Lovelace", "ada@example.com", "Math");
        when(studentRepository.existsByEmail(student.getEmail())).thenReturn(false);
        when(studentRepository.save(student)).thenReturn(student);

        assertSame(student, studentService.createStudent(student));
        verify(studentRepository).save(student);
    }

    @Test
    void createStudentRejectsDuplicateEmail() {
        Student student = new Student("Ada Lovelace", "ada@example.com", "Math");
        when(studentRepository.existsByEmail(student.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> studentService.createStudent(student));
        verify(studentRepository, never()).save(student);
    }

    @Test
    void updateStudentUpdatesAndSavesExistingStudent() {
        Student existing = new Student("Ada", "ada@example.com", "Math");
        Student updated = new Student("Ada Lovelace", "ada.l@example.com", "Science");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByEmail(updated.getEmail())).thenReturn(false);
        when(studentRepository.save(existing)).thenReturn(existing);

        Student result = studentService.updateStudent(1L, updated);

        assertSame(existing, result);
        assertEquals("Ada Lovelace", existing.getName());
        assertEquals("ada.l@example.com", existing.getEmail());
        assertEquals("Science", existing.getDepartment());
        verify(studentRepository).save(existing);
    }

    @Test
    void updateStudentRejectsEmailUsedByAnotherStudent() {
        Student existing = new Student("Ada", "ada@example.com", "Math");
        Student updated = new Student("Ada", "grace@example.com", "Math");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByEmail(updated.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> studentService.updateStudent(1L, updated));
        verify(studentRepository, never()).save(existing);
    }

    @Test
    void deleteStudentDeletesExistingStudent() {
        Student student = new Student("Ada", "ada@example.com", "Math");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void deleteStudentThrowsWhenMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class,
                () -> studentService.deleteStudent(1L));
        verify(studentRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }
}
