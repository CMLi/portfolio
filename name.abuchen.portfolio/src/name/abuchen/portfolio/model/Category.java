package name.abuchen.portfolio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Category
{
    private Category parent;

    private String name;
    private int percentage;

    private List<Category> children = new ArrayList<Category>();

    private List<Object> elements = new ArrayList<Object>();

    public Category()
    {}

    public Category(String name, int percentage)
    {
        this.name = name;
        this.percentage = percentage;
    }

    public Category getParent()
    {
        return parent;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPercentage()
    {
        return percentage;
    }

    public void setPercentage(int percentage)
    {
        this.percentage = percentage;
    }

    public List<Category> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public int getChildrenPercentage()
    {
        int sum = 0;
        for (Category c : children)
            sum += c.getPercentage();
        return sum;
    }

    public void addCategory(Category category)
    {
        category.parent = this;
        this.children.add(category);
    }

    public void removeCategory(Category category)
    {
        this.children.remove(category);
    }

    public List<Object> getElements()
    {
        return elements;
    }

    public List<Object> getTreeElements()
    {
        List<Object> answer = new ArrayList<Object>();

        Stack<Category> stack = new Stack<Category>();
        stack.push(this);

        while (!stack.isEmpty())
        {
            Category c = stack.pop();
            answer.addAll(c.elements);
            stack.addAll(c.children);
        }

        return answer;
    }

    public void addSecurity(Security security)
    {
        this.elements.add(security);
    }

    public void removeSecurity(Security security)
    {
        this.elements.remove(security);
    }

    public void addAccount(Account account)
    {
        this.elements.add(account);
    }

    public void removeAccount(Account account)
    {
        this.elements.remove(account);
    }
}
