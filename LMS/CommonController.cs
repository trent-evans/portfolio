/// <summary>
/// Retreive a JSON array of all departments from the database.
/// Each object in the array should have a field called "name" and "subject",
/// where "name" is the department name and "subject" is the subject abbreviation.
/// </summary>
/// <returns>The JSON array</returns>
public IActionResult GetDepartments()
{
using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = from dept in db.Departments
                    select new { name = dept.Name, subject = dept.SubjectAbrev };
        return Json(query.ToArray());
    }
}

/// <summary>
/// Returns a JSON array representing the course catalog.
/// Each object in the array should have the following fields:
/// "subject": The subject abbreviation, (e.g. "CS")
/// "dname": The department name, as in "Computer Science"
/// "courses": An array of JSON objects representing the courses in the department.
///            Each field in this inner-array should have the following fields:
///            "number": The course number (e.g. 5530)
///            "cname": The course name (e.g. "Database Systems")
/// </summary>
/// <returns>The JSON array</returns>
public IActionResult GetCatalog()
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = from d in db.Departments
                    select new
                    {
                        subject = d.SubjectAbrev,
                        dname = d.Name,
                        courses = from c in db.Courses.Where(c=>c.Department == d.SubjectAbrev) select new
                        {
                            number = c.CourseNum,
                            cname = c.CourseName
                        }
                    };
        return Json(query.ToArray());
    }        
}

/// <summary>
/// Returns a JSON array of all class offerings of a specific course.
/// Each object in the array should have the following fields:
/// "season": the season part of the semester, such as "Fall"
/// "year": the year part of the semester
/// "location": the location of the class
/// "start": the start time in format "hh:mm:ss"
/// "end": the end time in format "hh:mm:ss"
/// "fname": the first name of the professor
/// "lname": the last name of the professor
/// </summary>
/// <param name="subject">The subject abbreviation, as in "CS"</param>
/// <param name="number">The course number, as in 5530</param>
/// <returns>The JSON array</returns>
public IActionResult GetClassOfferings(string subject, int number)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = from c in db.Courses.Where(x=>x.Department == subject && x.CourseNum == number.ToString())
                    join cls in db.Classes on c.CatalogId equals cls.CatalogId into join1
                    from j1 in join1.DefaultIfEmpty()

                    join prof in db.Users on j1.ProfessorId equals prof.UId into join2
                    from j2 in join2.DefaultIfEmpty()

                    select new
                    {
                        // We stored our semester as "Semester Year" so we have to substring it to find
                        // "Semester" and "Year"
                        season = j1.Semester.Substring(0,j1.Semester.IndexOf(" ")),
                        year = j1.Semester.Substring(j1.Semester.IndexOf(" ")+1,4),
                        location = j1.Loc,
                        start = j1.StartTime,
                        end = j1.EndTime,
                        fname = j2.FirstName,
                        lname = j2.LastName
                    };
        return Json(query.ToArray());
    }
}

/// <summary>
/// This method does NOT return JSON. It returns plain text (containing html).
/// Use "return Content(...)" to return plain text.
/// Returns the contents of an assignment.
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class</param>
/// <param name="asgname">The name of the assignment in the category</param>
/// <returns>The assignment contents</returns>
public IActionResult GetAssignmentContents(string subject, int num, string season, int year, string category, string asgname)
{
    string semesterFull = season + " " + year.ToString();
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = (from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                    join cls in db.Classes.Where(x => x.Semester == semesterFull)
                    on course.CatalogId equals cls.CatalogId into join1

                    from j1 in join1.DefaultIfEmpty()
                    join assCat in db.AssignmentCategories.Where(x => x.Type == category)
                    on j1.CId equals assCat.CId into join2

                    from j2 in join2.DefaultIfEmpty()
                    join assign in db.Assignments.Where(x => x.Name == asgname)
                    on j2.AcId equals assign.AcId into join3

                    from j3 in join3.DefaultIfEmpty()
                    select j3.AssignContents).First();
        
        return Content(query); // The contents are a string, but we'll extra string it
    }
}


/// <summary>
/// This method does NOT return JSON. It returns plain text (containing html).
/// Use "return Content(...)" to return plain text.
/// Returns the contents of an assignment submission.
/// Returns the empty string ("") if there is no submission.
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class</param>
/// <param name="asgname">The name of the assignment in the category</param>
/// <param name="uid">The uid of the student who submitted it</param>
/// <returns>The submission text</returns>
public IActionResult GetSubmissionText(string subject, int num, string season, int year, string category, string asgname, string uid)
{
    string semesterFull = season + " " + year.ToString();
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = (from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                    join cls in db.Classes.Where(x => x.Semester == semesterFull)
                    on course.CatalogId equals cls.CatalogId into join1

                    from j1 in join1
                    join assCat in db.AssignmentCategories.Where(x => x.Type == category)
                    on j1.CId equals assCat.CId into join2

                    from j2 in join2
                    join assign in db.Assignments.Where(x => x.Name == asgname)
                    on j2.AcId equals assign.AcId into join3

                    from j3 in join3
                    join subs in db.Submissions.Where(x => x.StudentId == uid)
                    on j3.AId equals subs.AId into join4

                    from j4 in join4
                    select j4.SubsContents).First();

        return Content(query);
    }
}


/// <summary>
/// Gets information about a user as a single JSON object.
/// The object should have the following fields:
/// "fname": the user's first name
/// "lname": the user's last name
/// "uid": the user's uid
/// "department": (professors and students only) the name (such as "Computer Science") of the department for the user. 
///               If the user is a Professor, this is the department they work in.
///               If the user is a Student, this is the department they major in.    
///               If the user is an Administrator, this field is not present in the returned JSON
/// </summary>
/// <param name="uid">The ID of the user</param>
/// <returns>
/// The user JSON object 
/// or an object containing {success: false} if the user doesn't exist
/// </returns>
public IActionResult GetUser(string uid)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        // Figure out if the person is in the system or not, and what their role is
        string role = "NA";

        var studentCheck = from s in db.Students where s.UId == uid select s.Major;
        var profCheck = from p in db.Professors where p.UId == uid select p.Department;
        var adminCheck = from a in db.Administrators where a.UId == uid select a.UId;

        if (studentCheck.Count() == 1)
        {
            role = "S";
        }
        else if(profCheck.Count() == 1)
        {
            role = "P";
        }
        else if (adminCheck.Count() == 1)
        {
            role = "A";
        }
        
        if(role == "NA") // If we find nothing - bail out early
        {
            return Json(new { success = false });
        }
        else if(role == "S") // If it's a student
        {
            var query = from u in db.Users.Where(x => x.UId == uid)
                        select new
                        {
                            fname = u.FirstName,
                            lname = u.LastName,
                            uid = u.UId,
                            department = studentCheck.Single()
                        };
            var ret = query.ToArray();
            return Json(ret[0]);

        }else if(role == "P") // If it's a professor
        {
            var query = from u in db.Users.Where(x => x.UId == uid)
                        select new
                        {
                            fname = u.FirstName,
                            lname = u.LastName,
                            uid = u.UId,
                            department = profCheck.Single()
                        };
            var ret = query.ToArray();
            return Json(ret[0]);
        }
        else // Must be an administrator by default if it passed everything else
        {
            var query = from u in db.Users.Where(x => x.UId == uid)
                        select new
                        {
                            fname = u.FirstName,
                            lname = u.LastName,
                            uid = u.UId
                        };
            var ret = query.ToArray();
            return Json(ret[0]);
        }
    }
}