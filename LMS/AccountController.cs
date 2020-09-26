/// <summary>
/// Create a new user of the LMS with the specified information.
/// Assigns the user a unique uID consisting of a 'u' followed by 7 digits.
/// </summary>
/// <param name="fName">First Name</param>
/// <param name="lName">Last Name</param>
/// <param name="DOB">Date of Birth</param>
/// <param name="SubjectAbbrev">The department the user belongs to (professors and students only)</param>
/// <param name="SubjectAbbrev">The user's role: one of "Administrator", "Professor", "Student"</param> 
/// <returns>A unique uID that is not be used by anyone else</returns>
public string CreateNewUser(string fName, string lName, DateTime DOB, string SubjectAbbrev, string role)
{
        string newuID = "u"; // Initialize the new uID
        using (Team5LMSContext db = new Team5LMSContext())
        {
            // Build the uID
            int userQuant = (from u in db.Users select u.UId).Count(); // Get how many users there are

            int newIDNum;

              if(userQuant == 0)
            {
                newIDNum = 1;
            }
            else
            {
                var orderByUid = db.Users.OrderByDescending(x => x.UId);
                string lastUID = (from obu in orderByUid select obu.UId).First();
                newIDNum = Int32.Parse(lastUID.Substring(1)) + 1;
            }

            string idString = newIDNum.ToString();
            for(int x = 0; x < 7 - idString.Length; x++)
            {
                newuID += "0";
            }
            newuID += idString;

            // Create new user profile
            Users newUser = new Users();
            newUser.FirstName = fName;
            newUser.LastName = lName;
            newUser.Dob = DOB;
            newUser.UId = newuID;

            db.Users.Add(newUser);

            // Add to the applicable roster
            if(role == "Student")
            {
                Students newStudent = new Students();
                newStudent.UId = newuID;
                newStudent.Major = SubjectAbbrev;

                db.Students.Add(newStudent);
            }
            else if(role == "Professor")
            {
                Professors newProfessor = new Professors();
                newProfessor.UId = newuID;
                newProfessor.Department = SubjectAbbrev;

                db.Professors.Add(newProfessor);
            }else if(role == "Administrator")
            {
                Administrators newAdministrator = new Administrators();
                newAdministrator.UId = newuID;

                db.Administrators.Add(newAdministrator);
            }

            try
            { // Write out changes to the table
                db.SaveChanges();
            }
            catch(Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Failed to update user database");
                Console.WriteLine(ex.ToString()); // Print a stack trace
            }
        }
            
  return newuID;
}