# Learning Management System

A partner and I worked together to create the data editing, retreval, and storage methods for a Learning Management System (an idea similar to [Canvas by Instructure](https://www.instructure.com/canvas/)).  
<br>
For this project we used C#/.NET to write the methods, making particular use of the LINQ library for connecting to our database.  We used the Microsoft MVC model for building the code base.  Our work was to fill in methods for the controllers.  
<br>
In our LMS we have three kinds of users: [Andministrators](AdministratorController.cs), [Professors](ProfessorController.cs), and [Students](StudentController.cs).  As such, there are specific controllers for each role.  Beyond that there is a specific controller that [creates new accounts](AccountController.cs) and one for performing processes that are [general to all accounts](CommonController.cs).  
### An important note
What you see here are not complete files.  Because the files contained a lot of extra stuff (most of which we didn't do), I opted to remove the extra things and only leave in the methods that we wrote as a team.  