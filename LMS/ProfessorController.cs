/// <summary>
/// Returns a JSON array of all the students in a class.
/// Each object in the array should have the following fields:
/// "fname" - first name
/// "lname" - last name
/// "uid" - user ID
/// "dob" - date of birth
/// "grade" - the student's grade in this class
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <returns>The JSON array</returns>
public IActionResult GetStudentsInClass(string subject, int num, string season, int year)
{
        string fullSemester = season + " " + year.ToString();
        using(Team5LMSContext db = new Team5LMSContext())
        {
            var query = from cor in db.Courses.Where(x => x.Department == subject && x.CourseNum == num.ToString())
                        join cls in db.Classes.Where(x => x.Semester == fullSemester)
                        on cor.CatalogId equals cls.CatalogId into join1

                        from j1 in join1
                        join enr in db.Enrolled on j1.CId equals enr.CId into join2

                        from j2 in join2
                        join usr in db.Users on j2.UId equals usr.UId into join3

                        from j3 in join3
                        select new
                        {
                            fname = j3.FirstName,
                            lname = j3.LastName,
                            uid = j3.UId,
                            dob = j3.Dob,
                            grade = j2.Grade
                        };
            return Json(query.ToArray());
        }
}



/// <summary>
/// Returns a JSON array with all the assignments in an assignment category for a class.
/// If the "category" parameter is null, return all assignments in the class.
/// Each object in the array should have the following fields:
/// "aname" - The assignment name
/// "cname" - The assignment category name.
/// "due" - The due DateTime
/// "submissions" - The number of submissions to the assignment
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class, 
/// or null to return assignments from all categories</param>
/// <returns>The JSON array</returns>
public IActionResult GetAssignmentsInCategory(string subject, int num, string season, int year, string category)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            if (category == null) // Because reading is good
            {
                var query = from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                            join cls in db.Classes.Where(x => x.Semester == semesterFull)
                            on course.CatalogId equals cls.CatalogId into join1

                            from j1 in join1
                            join assignCat in db.AssignmentCategories
                            on j1.CId equals assignCat.CId into join2

                            from j2 in join2
                            join assign in db.Assignments
                            on j2.AcId equals assign.AcId into join3

                            from j3 in join3
                            select new
                            {
                                aname = j3.Name,
                                cname = j3.Type,
                                due = j3.Due,
                                submissions = (from subs in db.Submissions.Where(x => x.AId == j3.AId)
                                                select subs.StudentId).Count()
                            };
                return Json(query.ToArray());
            }
            else
            {
                var query = from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                            join cls in db.Classes.Where(x => x.Semester == semesterFull)
                            on course.CatalogId equals cls.CatalogId into join1

                            from j1 in join1
                            join assignCat in db.AssignmentCategories.Where(x => x.Type == category)
                            on j1.CId equals assignCat.CId into join2

                            from j2 in join2
                            join assign in db.Assignments
                            on j2.AcId equals assign.AcId into join3

                            from j3 in join3
                            select new
                            {
                                aname = j3.Name,
                                cname = j3.Type,
                                due = j3.Due,
                                submissions = (from subs in db.Submissions.Where(x => x.AId == j3.AId)
                                                select subs.StudentId).Count()
                            };
                return Json(query.ToArray());
            }
        }
}


/// <summary>
/// Returns a JSON array of the assignment categories for a certain class.
/// Each object in the array should have the following fields:
/// "name" - The category name
/// "weight" - The category weight
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <returns>The JSON array</returns>
public IActionResult GetAssignmentCategories(string subject, int num, string season, int year)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            var query = from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                        join cls in db.Classes.Where(x => x.Semester == semesterFull)
                        on course.CatalogId equals cls.CatalogId into join1

                        from j1 in join1
                        join assignCat in db.AssignmentCategories
                        on j1.CId equals assignCat.CId into join2

                        from j2 in join2
                        select new
                        {
                            name = j2.Type,
                            weight = j2.Weight
                        };

            return Json(query.ToArray());
        }
    }

/// <summary>
/// Creates a new assignment category for the specified class.
/// If a category of the given class with the given name already exists, return success = false.
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The new category name</param>
/// <param name="catweight">The new category weight</param>
/// <returns>A JSON object containing {success = true/false} </returns>
public IActionResult CreateAssignmentCategory(string subject, int num, string season, int year, string category, int catweight)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            int cIDQuery = (from cor in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                            join cls in db.Classes.Where(x => x.Semester == semesterFull)
                            on cor.CatalogId equals cls.CatalogId
                            select cls.CId).FirstOrDefault();

            int categoryCheck = (from ac in db.AssignmentCategories.Where(x => x.CId == cIDQuery && x.Type == category)
                                    select ac.CId).Count();

            if(categoryCheck != 0)
            {
                System.Diagnostics.Debug.WriteLine("Assignment category already exists!");
                return Json(new { success = false });
            }
            else
            {
                AssignmentCategories newAssignCat = new AssignmentCategories();

                
                var acOrdered = db.AssignmentCategories.OrderByDescending(x => x.AcId);
                if(acOrdered.Count() == 0)
                {
                    newAssignCat.AcId = 1;
                }
                else
                {
                    newAssignCat.AcId = (from acO in acOrdered select acO.AcId).First() + 1;
                }
                newAssignCat.Type = category;
                newAssignCat.Weight = catweight;
                newAssignCat.CId = cIDQuery;

                db.AssignmentCategories.Add(newAssignCat);

                try
                {
                    db.SaveChanges();
                    return Json(new { success = true });
                }
                catch(Exception ex)
                {
                    System.Diagnostics.Debug.WriteLine("Failed to write to the database");
                    return Json(new { success = false });
                }
            } 
        }
}

/// <summary>
/// Creates a new assignment for the given class and category.
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class</param>
/// <param name="asgname">The new assignment name</param>
/// <param name="asgpoints">The max point value for the new assignment</param>
/// <param name="asgdue">The due DateTime for the new assignment</param>
/// <param name="asgcontents">The contents of the new assignment</param>
/// <returns>A JSON object containing success = true/false</returns>
public IActionResult CreateAssignment(string subject, int num, string season, int year, string category, string asgname, int asgpoints, DateTime asgdue, string asgcontents)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            int AcIdQuery = (from cor in db.Courses.Where(x => x.Department == subject && x.CourseNum == num.ToString())
                        join cls in db.Classes.Where(x => x.Semester == semesterFull)
                        on cor.CatalogId equals cls.CatalogId into join1

                        from j1 in join1
                        join aC in db.AssignmentCategories.Where(x => x.Type == category)
                        on j1.CId equals aC.CId

                        select aC.AcId).First();

            int assignCheck = (from a in db.Assignments.Where(x => x.AcId == AcIdQuery && x.Name == asgname)
                                select a.AId).Count();

            if(assignCheck != 0)
            {
                System.Diagnostics.Debug.WriteLine("Assignment of that name already exists under category: " + category);
                return Json(new { success = false });
            }
            else
            {
                Assignments newAssignment = new Assignments();

                int aid;
                var assignOrdered = db.Assignments.OrderByDescending(x => x.AId);
                if(assignOrdered.Count() == 0)
                {
                    newAssignment.AId = 1;
                    aid = 1;
                }
                else
                {
                    int aidVal = (from aO in assignOrdered select aO.AId).First() + 1;
                    newAssignment.AId = aidVal;
                    aid = aidVal;
                }
                

                newAssignment.AcId = AcIdQuery;
                newAssignment.Type = category;
                newAssignment.Name = asgname;
                newAssignment.AssignContents = asgcontents;
                newAssignment.Due = asgdue;
                newAssignment.Points = asgpoints;

                db.Assignments.Add(newAssignment);

                try
                {
                    db.SaveChanges();
                    recalculateAllStudentGrades(subject, num, season, year);
                    return Json(new { success = true });
                }
                catch (Exception ex)
                {
                    System.Diagnostics.Debug.WriteLine("Failed to update assignments database");
                    Console.WriteLine(ex.ToString()); // Print a stack trace
                    return Json(new { success = false });
                }
            }    
        }
}


/// <summary>
/// Gets a JSON array of all the submissions to a certain assignment.
/// Each object in the array should have the following fields:
/// "fname" - first name
/// "lname" - last name
/// "uid" - user ID
/// "time" - DateTime of the submission
/// "score" - The score given to the submission
/// 
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class</param>
/// <param name="asgname">The name of the assignment</param>
/// <returns>The JSON array</returns>
public IActionResult GetSubmissionsToAssignment(string subject, int num, string season, int year, string category, string asgname)
{   
        using (Team5LMSContext db = new Team5LMSContext())
        {
            string semesterFull = season + " " + year.ToString();
            var query = (from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                        join cls in db.Classes.Where(x => x.Semester == semesterFull)
                        on course.CatalogId equals cls.CatalogId into join1

                        from j1 in join1
                        join assignCat in db.AssignmentCategories.Where(x => x.Type == category)
                        on j1.CId equals assignCat.CId into join2

                        from j2 in join2
                        join assign in db.Assignments.Where(x => x.Name == asgname)
                        on j2.AcId equals assign.AcId
                        select assign.AId);

            var query2 = from subs in db.Submissions.Where(x => x.AId == query.First())
                            select subs;

            var query3 = from subs in query2
                            join us in db.Users on subs.StudentId equals us.UId
                            select new
                            {
                                fname = us.FirstName,
                                lname = us.LastName,
                                uid = us.UId,
                                time = subs.SubmissionTime,
                                //score = subs.Score
                                score = subs.Score == null ? null : (uint?)subs.Score
                            };

            return Json(query3.ToArray());
        }  
}


/// <summary>
/// Set the score of an assignment submission
/// </summary>
/// <param name="subject">The course subject abbreviation</param>
/// <param name="num">The course number</param>
/// <param name="season">The season part of the semester for the class the assignment belongs to</param>
/// <param name="year">The year part of the semester for the class the assignment belongs to</param>
/// <param name="category">The name of the assignment category in the class</param>
/// <param name="asgname">The name of the assignment</param>
/// <param name="uid">The uid of the student who's submission is being graded</param>
/// <param name="score">The new score for the submission</param>
/// <returns>A JSON object containing success = true/false</returns>
public IActionResult GradeSubmission(string subject, int num, string season, int year, string category, string asgname, string uid, int score)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            int getAId = (from course in db.Courses.Where(x => x.CourseNum == num.ToString() && x.Department == subject)
                            join cls in db.Classes.Where(x => x.Semester == semesterFull)
                            on course.CatalogId equals cls.CatalogId into join1

                            from j1 in join1
                            join assignCat in db.AssignmentCategories.Where(x => x.Type == category)
                            on j1.CId equals assignCat.CId into join2

                            from j2 in join2
                            join assign in db.Assignments.Where(x => x.Name == asgname)
                            on j2.AcId equals assign.AcId

                            select assign.AId).First();

            Submissions studentSubmission = db.Submissions.Single(x => x.StudentId == uid && x.AId == getAId);
            studentSubmission.Score = score;

            try
            {
                db.SaveChanges();
                recalculateStudentGrade(subject, num, season, year, uid);
                return Json(new { success = true });
            }
            catch(Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Failed to update submission score");
                Console.WriteLine(ex.ToString()); // Print a stack trace
                return Json(new { success = false });
            }
        }
}


/// <summary>
/// Returns a JSON array of the classes taught by the specified professor
/// Each object in the array should have the following fields:
/// "subject" - The subject abbreviation of the class (such as "CS")
/// "number" - The course number (such as 5530)
/// "name" - The course name
/// "season" - The season part of the semester in which the class is taught
/// "year" - The year part of the semester in which the class is taught
/// </summary>
/// <param name="uid">The professor's uid</param>
/// <returns>The JSON array</returns>
public IActionResult GetMyClasses(string uid)
{            
        using (Team5LMSContext db = new Team5LMSContext())
        {
            var query = from cls in db.Classes.Where(x => x.ProfessorId == uid)
                        join cour in db.Courses 
                        on cls.CatalogId equals cour.CatalogId
                        select new
                        {
                            subject = cour.Department,
                            number = cour.CourseNum,
                            name = cour.CourseName,
                            season = cls.Semester.Substring(0, cls.Semester.IndexOf(" ")),
                            year = cls.Semester.Substring(cls.Semester.IndexOf(" ") + 1, 4),
                        };
            return Json(query.ToArray()); 
        }
}

public void recalculateAllStudentGrades(string subject, int num, string season, int year)
    {
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            int cid = (from cor in db.Courses.Where(x => x.Department == subject && x.CourseNum == num.ToString())
                            join cls in db.Classes.Where(x => x.Semester == semesterFull)
                            on cor.CatalogId equals cls.CatalogId
                            select cls.CId).First();

            var studentsInClass = from en in db.Enrolled.Where(x => x.CId == cid) select new { id = en.UId };
            foreach(var student in studentsInClass)
            {
                recalculateStudentGrade(subject, num, season, year, student.id);
            }
        }
    }

public void recalculateStudentGrade(string subject, int num, string season, int year, string uid)
{
        string semesterFull = season + " " + year.ToString();
        using (Team5LMSContext db = new Team5LMSContext())
        {
            int cid = (from cor in db.Courses.Where(x => x.Department == subject && x.CourseNum == num.ToString())
                        join cls in db.Classes.Where(x => x.Semester == semesterFull)
                        on cor.CatalogId equals cls.CatalogId
                        select cls.CId).First();

            var assignCat = from cls in db.Classes.Where(x => x.CId == cid)
                            join ac in db.AssignmentCategories
                            on cls.CId equals ac.CId
                            select new { category = ac.Type, acID = ac.AcId, weight = ac.Weight };

            int totalWeight = 0;
            List<double> categoryWeightByPercentage = new List<double>();

            foreach(var cat in assignCat) // Loop through each assignment category
            {
                double categoryTotal = 0; 
                double categoryScore = 0;

                var assignments = from aC in db.AssignmentCategories.Where(x => x.AcId == cat.acID && x.Type == cat.category)
                                    join a in db.Assignments
                                    on aC.AcId equals a.AcId into join1

                                    from j1 in join1
                                    join s in db.Submissions.Where(x => x.StudentId == uid)
                                    on j1.AId equals s.AId
                                    // Pull the scores out from each assignment 
                                    select new { totalPoints = j1.Points, score = s.Score };

                foreach(var assign in assignments) // Get total points/score for categories
                {
                    categoryTotal += (double)assign.totalPoints;
                    categoryScore += (double)assign.score;
                }
                if (assignments.Count() != 0) // Account for empty categories
                {
                    totalWeight += cat.weight;
                    categoryWeightByPercentage.Add((categoryScore / categoryTotal) * cat.weight);
                }                  
            }

            double categoryWeightPercentSum = categoryWeightByPercentage.Sum();
            double scalingFactor = 100 / totalWeight;
            double finalPercent = categoryWeightPercentSum * scalingFactor;

            string[] grades = { "A", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "E" };
            double[] points = { 100, 93, 90, 87, 83, 80, 77, 73, 70, 67, 63, 60, 0 };
            string grade = "--";

            for (int x = 1; x < grades.Length; x++)
            {
                if (finalPercent > points[x] && finalPercent < points[x - 1])
                {
                    grade = grades[x];
                    break;
                }
            }
            Enrolled enChangeGrade = db.Enrolled.Single(x => x.CId == cid && x.UId == uid);
            enChangeGrade.Grade = grade;
            try
            {
                db.SaveChanges();
            }
            catch(Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Failed to update student grade");
                Console.WriteLine(ex.ToString()); // Print a stack trace
            }
            
        }
}