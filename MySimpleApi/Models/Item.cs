using System.ComponentModel.DataAnnotations;

namespace MySimpleApi.Models
{
    public class Item
    {
        public int Id { get; set; }
        
        [Required(ErrorMessage = "Name is required")]
        [StringLength(100, MinimumLength = 1, ErrorMessage = "Name must be between 1 and 100 characters")]
        public string? Name { get; set; }
        
        public bool IsComplete { get; set; }
        
        [DataType(DataType.Date)]
        public DateTime? CreatedDate { get; set; } = DateTime.UtcNow;
    }
}