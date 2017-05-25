using System;
using System.Globalization;
using System.Threading;
using System.Windows.Forms;

namespace ChangeLang
{
    public partial class perInfoForm : Form
    {
        public perInfoForm()
        {
            InitializeComponent();
        }

        private void Localize()
        {
            fstNameLabel.Text = GlobalEntities.firstName;
            lastNameLabel.Text = GlobalEntities.lastName;
            ageLabel.Text = GlobalEntities.age;
            registerButton.Text = GlobalEntities.registerButton;
            title.Text = GlobalEntities.title;
            this.Text = GlobalEntities.formName;
            if (Thread.CurrentThread.CurrentCulture.Name == "ru")
            {
                title.Font = new System.Drawing.Font(title.Font.Name, 14);                
                registerButton.Width = 90;
            }
            if (Thread.CurrentThread.CurrentCulture.Name == "en")
            {
                title.Font = new System.Drawing.Font(title.Font.Name, 18);
                registerButton.Width = 67;
            }
        }

        private void perInfoForm_Load(object sender, EventArgs e)
        {

        }

        private void ukIco_Click(object sender, EventArgs e)
        {
            Thread.CurrentThread.CurrentCulture = new CultureInfo("en");
            Thread.CurrentThread.CurrentUICulture = new CultureInfo("en");
            Localize();
        }

        private void russiaIco_Click(object sender, EventArgs e)
        {
            Thread.CurrentThread.CurrentCulture = new CultureInfo("ru");
            Thread.CurrentThread.CurrentUICulture = new CultureInfo("ru");
            Localize();
        }
    }
}
