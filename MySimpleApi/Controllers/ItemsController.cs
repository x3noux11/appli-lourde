using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using MySimpleApi.Models;
using System;
using System.Collections.Generic;
using System.Linq;

namespace MySimpleApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    public class ItemsController : ControllerBase
    {
        private static readonly List<Item> _items = new List<Item>
        {
            new Item { Id = 1, Name = "Walk dog", IsComplete = false },
            new Item { Id = 2, Name = "Do laundry", IsComplete = true }
        };
        
        private readonly ILogger<ItemsController> _logger;

        public ItemsController(ILogger<ItemsController> logger)
        {
            _logger = logger;
        }

        // GET: api/items
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<Item>), 200)]
        public ActionResult<IEnumerable<Item>> GetItems()
        {
            _logger.LogInformation("Getting all items at {Time}", DateTime.UtcNow);
            return _items;
        }

        // GET: api/items/1
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(Item), 200)]
        [ProducesResponseType(404)]
        public ActionResult<Item> GetItem(int id)
        {
            _logger.LogInformation("Getting item with ID: {Id} at {Time}", id, DateTime.UtcNow);
            var item = _items.FirstOrDefault(i => i.Id == id);

            if (item == null)
            {
                _logger.LogWarning("Item with ID: {Id} not found", id);
                return NotFound();
            }

            return item;
        }

        // POST: api/items
        [HttpPost]
        [ProducesResponseType(typeof(Item), 201)]
        [ProducesResponseType(400)]
        public ActionResult<Item> PostItem(Item item)
        {
            if (string.IsNullOrWhiteSpace(item.Name))
            {
                _logger.LogWarning("Attempted to create item with invalid name");
                return BadRequest("Name is required");
            }

            item.Id = _items.Any() ? _items.Max(i => i.Id) + 1 : 1;
            _items.Add(item);
            
            _logger.LogInformation("Created new item with ID: {Id}", item.Id);
            // Returns a 201 Created status code, the location of the new resource, and the new item itself.
            return CreatedAtAction(nameof(GetItem), new { id = item.Id }, item);
        }

        // PUT: api/items/1
        [HttpPut("{id}")]
        [ProducesResponseType(204)]
        [ProducesResponseType(400)]
        [ProducesResponseType(404)]
        public IActionResult PutItem(int id, Item item)
        {
            if (id != item.Id)
            {
                _logger.LogWarning("ID mismatch in PUT request. URL ID: {UrlId}, Item ID: {ItemId}", id, item.Id);
                return BadRequest("ID in URL must match ID in request body");
            }

            if (string.IsNullOrWhiteSpace(item.Name))
            {
                _logger.LogWarning("Attempted to update item with invalid name");
                return BadRequest("Name is required");
            }

            var existingItem = _items.FirstOrDefault(i => i.Id == id);
            if (existingItem == null)
            { 
                _logger.LogWarning("Item with ID: {Id} not found for update", id);
                return NotFound();
            }

            existingItem.Name = item.Name;
            existingItem.IsComplete = item.IsComplete;

            _logger.LogInformation("Updated item with ID: {Id}", id);
            return NoContent(); // Returns a 204 No Content status code
        }

        // DELETE: api/items/1
        [HttpDelete("{id}")]
        [ProducesResponseType(204)]
        [ProducesResponseType(404)]
        public IActionResult DeleteItem(int id)
        {
            var item = _items.FirstOrDefault(i => i.Id == id);
            if (item == null)
            {
                _logger.LogWarning("Item with ID: {Id} not found for deletion", id);
                return NotFound();
            }

            _items.Remove(item);
            _logger.LogInformation("Deleted item with ID: {Id}", id);

            return NoContent(); // Returns a 204 No Content status code
        }
    }
}