/// <summary>
/// Returns a JSON array of all the courses in the given department.
/// Each object in the array should have the following fields:
/// "number" - The course number (as in 5530)
/// "name" - The course name (as in "Database Systems")
/// </summary>
/// <param name="subject">The department subject abbreviation (as in "CS")</param>
/// <returns>The JSON result</returns>
public IActionResult GetCourses(string subject)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = from c in db.Courses.Where(c => c.Department == subject)
                    select new
                    {
                        number = c.CourseNum,
                        name = c.CourseName
                    };
                    
        return Json(query.ToArray());
    }
}

/// <summary>
/// Returns a JSON array of all the professors working in a given department.
/// Each object in the array should have the following fields:
/// "lname" - The professor's last name
/// "fname" - The professor's first name
/// "uid" - The professor's uid
/// </summary>
/// <param name="subject">The department subject abbreviation</param>
/// <returns>The JSON result</returns>
public IActionResult GetProfessors(string subject)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var query = from d in db.Departments.Where(d => d.SubjectAbrev == subject)
                    join p in db.Professors on d.SubjectAbrev equals p.Department into join1
                    from j1 in join1
                    join u in db.Users on j1.UId equals u.UId into join2
                    from j2 in join2
                    select new
                    {
                        lname = j2.LastName,
                        fname = j2.FirstName,
                        uid = j2.UId
                    };
        return Json(query.ToArray());
    }
}

/// <summary>
/// Creates a course.
/// A course is uniquely identified by its number + the subject to which it belongs
/// </summary>
/// <param name="subject">The subject abbreviation for the department in which the course will be added</param>
/// <param name="number">The course number</param>
/// <param name="name">The course name</param>
/// <returns>A JSON object containing {success = true/false}.
/// false if the course already exists, true otherwise.</returns>
public IActionResult CreateCourse(string subject, int number, string name)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        var sup = GetCourses(subject);
        int courseCount = (from c in db.Courses
                            orderby c.CatalogId descending
                            select c.CatalogId).Count();
        var temp = courseCount + 1;
        String catid = temp.ToString();
        //consider sorting in descending order, take biggest, add one to that.
        Courses course = new Courses();
        course.CourseName = name;
        string intString = number.ToString();
        course.CourseNum = intString;
        course.Department = subject;
        course.CatalogId = catid;
        db.Courses.Add(course);
        try
        { // Write out changes to the table
            db.SaveChanges();
            return Json(new { success = true });
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine("Failed to update course database");
            Console.WriteLine(ex.ToString()); // Print a stack trace
            return Json(new { success = false });
        }
    }
}

/// <summary>
/// Creates a class offering of a given course.
/// </summary>
/// <param name="subject">The department subject abbreviation</param>
/// <param name="number">The course number</param>
/// <param name="season">The season part of the semester</param>
/// <param name="year">The year part of the semester</param>
/// <param name="start">The start time</param>
/// <param name="end">The end time</param>
/// <param name="location">The location</param>
/// <param name="instructor">The uid of the professor</param>
/// <returns>A JSON object containing {success = true/false}. 
/// false if another class occupies the same location during any time 
/// within the start-end range in the same semester, or if there is already
/// a Class offering of the same Course in the same Semester,
/// true otherwise.</returns>
public IActionResult CreateClass(string subject, int number, string season, int year, DateTime start, DateTime end, string location, string instructor)
{
    using (Team5LMSContext db = new Team5LMSContext())
    {
        // Chcking location and times
        string semester = season + " " + year.ToString();
        var query = from cls in db.Classes.Where(x => x.Semester == semester && x.Loc == location)
                    select new { startTime = cls.StartTime, endTime = cls.EndTime };

        foreach(var cls in query)
        {
            int startStartCompare = DateTime.Compare(start, cls.startTime.Value);
            int startEndCompare = DateTime.Compare(start, cls.endTime.Value);
            int endStartCompare = DateTime.Compare(end, cls.startTime.Value);
            int endEndCompare = DateTime.Compare(end, cls.endTime.Value);

            // If it starts after another class, but not before the other class ends
            //      startStart > 0,       startEnd < 0
            if(startStartCompare > 0 && startEndCompare < 0)
            {
                return Json(new { success = false });
            } // If it starts before another class, but ends during another class
            else if(startStartCompare < 0 && endEndCompare <= 0) 
            {
                return Json(new { success = false });
            } // If it starts in the middle of a class and ends in the middle of a class
            else if(endStartCompare > 0 && endEndCompare < 0)
            {
                return Json(new { success = false });
            } 
        }

        string catalogID = (from cor in db.Courses.Where(x => x.CourseNum == number.ToString()
                                && x.Department == subject)
                            select cor.CatalogId).First();

        var query2 = from cls in db.Classes.Where(x => x.CatalogId == catalogID && x.Semester == semester)
                        select cls.CId;
        if(query2.Count() != 0)
        {
            return Json(new { success = false });
        }

        int cID = (from c in db.Classes
                        orderby c.CId descending
                        select c.CId).First() + 1;


        Classes newClass = new Classes();
        newClass.CId = cID;
        newClass.ProfessorId = instructor;
        newClass.StartTime = start;
        newClass.EndTime = end;
        newClass.Loc = location;
        newClass.Semester = semester;
        newClass.CatalogId = (from cor in db.Courses.Where(x => x.CourseNum == number.ToString() 
                                && x.Department == subject) select cor.CatalogId).First().ToString();
        db.Classes.Add(newClass);
        try
        { // Write out changes to the table
            db.SaveChanges();
            return Json(new { success = true });
        }
        catch (Exception ex)
        {
            System.Diagnostics.Debug.WriteLine("Failed to update class database");
            Console.WriteLine(ex.ToString()); // Print a stack trace
            return Json(new { success = false });
        }     
    }
}