/// <summary>
/// Returns a JSON array of the classes the given student is enrolled in.
/// Each object in the array should have the following fields:
/// "subject" - The subject abbreviation of the class (such as "CS")
/// "number" - The course number (such as 5530)
/// "name" - The course name
/// "season" - The season part of the semester
/// "year" - The year part of the semester
/// "grade" - The grade earned in the class, or "--" if one hasn't been assigned
/// </summary>
/// <param name="uid">The uid of the student</param>
/// <returns>The JSON array</returns>
public IActionResult GetMyClasses(string uid)
{
        using (Team5LMSContext db = new Team5LMSContext())
        {
            var query = from s in db.Students.Where(s => s.UId == uid)
                        join e in db.Enrolled on s.UId equals e.UId into join1
                        from j1 in join1
                        join c in db.Classes on j1.CId equals c.CId into join2
                        from j2 in join2
                        join co in db.Courses on j2.CatalogId equals co.CatalogId into join3
                        from j3 in join3
                        select new
                        {
                            subject = j3.Department,
                            number = j3.CourseNum,
                            name = j3.CourseName,
                            season = j2.Semester.Substring(0, j2.Semester.IndexOf(" ")),
                            year = j2.Semester.Substring(j2.Semester.IndexOf(" ") + 1, 4),
                            grade = j1.Grade
                        };
            return Json(query.ToArray());
        }
    }

    /// <summary>
    /// Returns a JSON array of all the assignments in the given class that the given student is enrolled in.
    /// Each object in the array should have the following fields:
    /// "aname" - The assignment name
    /// "cname" - The category name that the assignment belongs to
    /// "due" - The due Date/Time
    /// "score" - The score earned by the student, or null if the student has not submitted to this assignment.
    /// </summary>
    /// <param name="subject">The course subject abbreviation</param>
    /// <param name="num">The course number</param>
    /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
    /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
    /// <param name="uid"></param>
    /// <returns>The JSON array</returns>
    public IActionResult GetAssignmentsInClass(string subject, int num, string season, int year, string uid)
{
        using (Team5LMSContext db = new Team5LMSContext())
        {
            string Semester = season + " " + year;

            var query = from cor in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                        join cls in db.Classes.Where(x => x.Semester == Semester) on cor.CatalogId equals cls.CatalogId
                        select cls;

            var query2 = from j1 in query
                            join assCat in db.AssignmentCategories on j1.CId equals assCat.CId
                            select assCat;

            var query3 = from j2 in query2
                            join assign in db.Assignments on j2.AcId equals assign.AcId
                            select assign;

            // Thanks Varun
            var query4 = from q in query3
                            join s in db.Submissions
                            on new { A = q.AId, B = uid } equals new { A = s.AId, B = s.StudentId }
                            into joined
                            from j in joined.DefaultIfEmpty()
                            select new
                            {
                                aname = q.Name,
                                cname = q.Type,
                                due = q.Due,
                                score = j.Score == null ? null : (uint?)j.Score
                            };

            return Json(query4.ToArray());
        }
    }

    


        /// <summary>
        /// Adds a submission to the given assignment for the given student
        /// The submission should use the current time as its DateTime
        /// You can get the current time with DateTime.Now
        /// The score of the submission should start as 0 until a Professor grades it
        /// If a Student submits to an assignment again, it should replace the submission contents
        /// and the submission time (the score should remain the same).
        /// </summary>
        /// <param name="subject">The course subject abbreviation</param>
        /// <param name="num">The course number</param>
        /// <param name="season">The season part of the semester for the class the assignment belongs to</param>
        /// <param name="year">The year part of the semester for the class the assignment belongs to</param>
        /// <param name="category">The name of the assignment category in the class</param>
        /// <param name="asgname">The new assignment name</param>
        /// <param name="uid">The student submitting the assignment</param>
        /// <param name="contents">The text contents of the student's submission</param>
        /// <returns>A JSON object containing {success = true/false}</returns>
        public IActionResult SubmitAssignmentText(string subject, int num, string season, int year, 
    string category, string asgname, string uid, string contents)
{
        using (Team5LMSContext db = new Team5LMSContext())
        {

            String semesterFull = season + " " + year;
            var query = from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                        join cls in db.Classes.Where(x => x.Semester == semesterFull)
                        on course.CatalogId equals cls.CatalogId into join1
                        from j1 in join1
                        join assCat in db.AssignmentCategories.Where(x => x.Type == category)
                        on j1.CId equals assCat.CId into join2
                        from j2 in join2
                        join assign in db.Assignments.Where(x => x.Name == asgname)
                        on j2.AcId equals assign.AcId into join3
                        from j3 in join3
                        select new
                        {
                            stuff = j3.AId
                        };

            var subsCheck = from subs in db.Submissions.Where(x => x.AId == query.First().stuff && x.StudentId == uid)
                            select subs;
            if(subsCheck.Count() == 0) // If it doesn't exist yet
            {
                Submissions submission = new Submissions();
                submission.StudentId = uid;
                submission.AId = query.First().stuff;
                submission.Score = 0;
                submission.SubsContents = contents;
                submission.SubmissionTime = DateTime.Now;

                db.Submissions.Add(submission);
            }
            else // Otherwise update the oroginal thing
            {
                Submissions submission = db.Submissions.Single(x => x.AId == query.First().stuff && x.StudentId == uid);
                submission.SubsContents = contents;
                submission.SubmissionTime = DateTime.Now;
            }
            
            try
            {
                db.SaveChanges();
                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Failed to update submission text");
                Console.WriteLine(ex.ToString()); // Print a stack trace
                return Json(new { success = false });
            }
        }
    }


    /// <summary>
    /// Enrolls a student in a class.
    /// </summary>
    /// <param name="subject">The department subject abbreviation</param>
    /// <param name="num">The course number</param>
    /// <param name="season">The season part of the semester</param>
    /// <param name="year">The year part of the semester</param>
    /// <param name="uid">The uid of the student</param>
    /// <returns>A JSON object containing {success = {true/false}. 
    /// false if the student is already enrolled in the class, true otherwise.</returns>
    public IActionResult Enroll(string subject, int num, string season, int year, string uid)
{
        using (Team5LMSContext db = new Team5LMSContext())
        {
            string semester = season + " " + year.ToString();
            var cidGrab = from cor in db.Courses.Where(x => x.Department == subject && x.CourseNum == num.ToString())
                            join cls in db.Classes.Where(x => x.Semester == semester)
                            on cor.CatalogId equals cls.CatalogId
                            select new { cid = cls.CId };

            
            Enrolled enroll = new Enrolled();
            enroll.UId = uid;
            enroll.Grade = "--";
            enroll.CId = cidGrab.First().cid;
            db.Add(enroll);
            try
            {
                db.SaveChanges();
                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Failed to update enrolled");
                Console.WriteLine(ex.ToString()); // Print a stack trace
                return Json(new { success = false });
            }
        }
    }



/// <summary>
/// Calculates a student's GPA
/// A student's GPA is determined by the grade-point representation of the average grade in all their classes.
/// Assume all classes are 4 credit hours.
/// If a student does not have a grade in a class ("--"), that class is not counted in the average.
/// If a student is not enrolled in any classes, they have a GPA of 0.0.
/// Otherwise, the point-value of a letter grade is determined by the table on this page:
/// https://advising.utah.edu/academic-standards/gpa-calculator-new.php
/// </summary>
/// <param name="uid">The uid of the student</param>
/// <returns>A JSON object containing a single field called "gpa" with the number value</returns>
public IActionResult GetGPA(string uid)
{
        string[] grades = { "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "E" };
        double[] points = { 4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7, 1.3, 1.0, 0.7, 0.0};

        Dictionary<string, double> gradeToPoints = new Dictionary<string, double>();
        for(int x = 0; x < grades.Length; x++)
        {
            gradeToPoints.Add(grades[x], points[x]);
        }

        double gpaValue = 0.0;
        using (Team5LMSContext db = new Team5LMSContext())
        {
            var query = from en in db.Enrolled.Where(x => x.UId == uid)
                        select en.Grade;

            if(query.Count() == 0) // If there's no classes, bounce early
            {
                return Json(new { gpa = gpaValue });
            }

            double totalGrades = 0;
            foreach(string grade in query)
            {
                if(grade == "--")
                {
                    continue;
                }
                else
                {
                    totalGrades += 1;
                    gpaValue += gradeToPoints[grade];
                }
            }

            if(totalGrades == 0)
            {
                return Json(new { gpa = gpaValue });
            }
            else
            {
                return Json(new { gpa = (gpaValue / totalGrades) });
            } 
        };
}